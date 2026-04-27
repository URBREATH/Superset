package eu.urbreathdsjobs.client.frost;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
public class FrostClientService {

    private final SensorThingsService sensorThingsService;
    private final FrostProperties frostProperties;

    public List<FrostProperties.DatastreamConfig> getConfiguredDatastreams() {
        return frostProperties.getEnabledDatastreams();
    }

    public Datastream findDatastream(Long datastreamId) {
        try {
            return sensorThingsService.datastreams().find(datastreamId);
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to load FROST datastream " + datastreamId, ex);
        }
    }

    public List<Observation> fetchObservations(FrostProperties.DatastreamConfig datastreamConfig) {
        validate(datastreamConfig);

        ObservationDao observationDao = sensorThingsService.observations();
        Query query = observationDao.query();

        String filter = "Datastream/id eq " + datastreamConfig.getDatastreamId();
        if (StringUtils.hasText(datastreamConfig.getFilter())) {
            filter = "(" + filter + ") and (" + datastreamConfig.getFilter() + ")";
        }

        query.filter(filter);

        if (StringUtils.hasText(frostProperties.getOrderBy())) {
            query.orderBy(frostProperties.getOrderBy());
        }
        if (frostProperties.getPageSize() != null && frostProperties.getPageSize() > 0) {
            query.top(frostProperties.getPageSize());
        }
        if (StringUtils.hasText(frostProperties.getExpand())) {
            query.expand(frostProperties.getExpand());
        }

        try {
            @SuppressWarnings("unchecked")
            EntityList<Observation> entityList = query.list();
            List<Observation> observations = new ArrayList<>();
            Iterator<Observation> iterator = entityList.fullIterator();
            while (iterator.hasNext()) {
                observations.add(iterator.next());
            }

            log.info(
                    "Fetched {} FROST observations for datastreamId={}, city={}, observedProperty={}",
                    observations.size(),
                    datastreamConfig.getDatastreamId(),
                    datastreamConfig.getCity(),
                    datastreamConfig.getObservedProperty()
            );
            return observations;
        } catch (ServiceFailureException ex) {
            throw new RuntimeException(
                    "Unable to fetch FROST observations for datastream " + datastreamConfig.getDatastreamId(),
                    ex
            );
        }
    }

    private void validate(FrostProperties.DatastreamConfig datastreamConfig) {
        if (datastreamConfig == null) {
            throw new IllegalArgumentException("Datastream configuration must not be null");
        }
        if (datastreamConfig.getDatastreamId() == null) {
            throw new IllegalArgumentException("Missing frost.datastreams[].datastream-id");
        }
        if (datastreamConfig.getSensorId() == null) {
            throw new IllegalArgumentException("Missing frost.datastreams[].sensor-id for datastream " + datastreamConfig.getDatastreamId());
        }
    }
}

