package eu.urbreathdsjobs.client.frost;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
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
import java.util.function.IntFunction;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
public class FrostClientService {

    private final SensorThingsService sensorThingsService;
    private final FrostProperties frostProperties;

    public List<FrostProperties.DatastreamConfig> getConfiguredDatastreams() {
        List<FrostProperties.DatastreamConfig> configuredDatastreams = frostProperties.getEnabledDatastreams();
        if (!configuredDatastreams.isEmpty()) {
            return configuredDatastreams;
        }

        log.info("No enabled frost.datastreams configured. Falling back to datastream discovery from FROST server.");
        return fetchDatastreamConfigsFromServer();
    }


    public Datastream findDatastream(Long datastreamId) {
        try {
            return sensorThingsService.datastreams().find(datastreamId);
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to load FROST datastream " + datastreamId, ex);
        }
    }

    public Thing findThing(Long thingId) {
        try {
            return sensorThingsService.things().find(thingId);
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to load FROST thing " + thingId, ex);
        }
    }

    public Sensor findSensor(Long sensorId) {
        try {
            return sensorThingsService.sensors().find(sensorId);
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to load FROST sensor " + sensorId, ex);
        }
    }

    public ObservedProperty findObservedProperty(Long observedPropertyId) {
        try {
            return sensorThingsService.observedProperties().find(observedPropertyId);
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to load FROST observedProperty " + observedPropertyId, ex);
        }
    }

    public FeatureOfInterest findFeatureOfInterest(Long featureOfInterestId) {
        try {
            return sensorThingsService.featuresOfInterest().find(featureOfInterestId);
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to load FROST featureOfInterest " + featureOfInterestId, ex);
        }
    }

    public Observation findObservation(Long observationId) {
        try {
            return sensorThingsService.observations().find(observationId);
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to load FROST observation " + observationId, ex);
        }
    }

    public List<Observation> fetchObservations(FrostProperties.DatastreamConfig datastreamConfig) {
        validate(datastreamConfig);
        Query<Observation> query = baseObservationQuery(datastreamConfig);
        if (frostProperties.getPageSize() != null && frostProperties.getPageSize() > 0) {
            query.top(frostProperties.getPageSize());
        }

        try {
            EntityList<Observation> entityList = query.list();
            Iterator<Observation> iterator = frostProperties.isFollowPaginationLinks()
                    ? entityList.fullIterator()
                    : entityList.iterator();
            List<Observation> observations = toList(iterator);

            log.info(
                    "Fetched {} FROST observations for datastreamId={}, city={}, observedProperty={}, followPaginationLinks={}",
                    observations.size(),
                    datastreamConfig.getDatastreamId(),
                    datastreamConfig.getCity(),
                    datastreamConfig.getObservedProperty(),
                    frostProperties.isFollowPaginationLinks()
            );
            return observations;
        } catch (ServiceFailureException ex) {
            throw new RuntimeException(
                    "Unable to fetch FROST observations for datastream " + datastreamConfig.getDatastreamId(),
                    ex
            );
        }
    }

    public List<Observation> fetchObservationsPage(FrostProperties.DatastreamConfig datastreamConfig, int pageIndex) {
        validate(datastreamConfig);
        validatePageIndex(pageIndex);

        int pageSize = resolvePageSize();
        Query<Observation> query = baseObservationQuery(datastreamConfig);
        query.top(pageSize);
        query.skip(Math.multiplyExact(pageIndex, pageSize));

        try {
            EntityList<Observation> entityList = query.list();
            List<Observation> observations = toList(entityList.iterator());

            log.info(
                    "Fetched page {} (size={}) with {} FROST observations for datastreamId={}, city={}, observedProperty={}",
                    pageIndex,
                    pageSize,
                    observations.size(),
                    datastreamConfig.getDatastreamId(),
                    datastreamConfig.getCity(),
                    datastreamConfig.getObservedProperty()
            );
            return observations;
        } catch (ServiceFailureException ex) {
            throw new RuntimeException(
                    "Unable to fetch FROST observation page " + pageIndex + " for datastream " + datastreamConfig.getDatastreamId(),
                    ex
            );
        }
    }

    public int countObservationPages(FrostProperties.DatastreamConfig datastreamConfig) {
        validate(datastreamConfig);
        return countPages(pageIndex -> fetchObservationsPage(datastreamConfig, pageIndex));
    }

    /**
     * Conta il totale delle osservazioni sul server FROST per un singolo datastream
     * usando $count=true&$top=0: una sola chiamata HTTP, nessun dato scaricato.
     * Ritorna null se il server non supporta $count.
     */
    public Long countObservationsTotal(FrostProperties.DatastreamConfig datastreamConfig) {
        if (datastreamConfig == null || datastreamConfig.getDatastreamId() == null) {
            return null;
        }
        try {
            Query<Observation> query = baseObservationQuery(datastreamConfig);
            query.count();
            query.top(0);
            EntityList<Observation> result = query.list();
            return result.getCount();
        } catch (ServiceFailureException ex) {
            log.warn("Unable to count observations for datastreamId={}: {}", datastreamConfig.getDatastreamId(), ex.getMessage());
            return null;
        }
    }

    public List<Datastream> fetchDatastreamsPage(int pageIndex) {
        validatePageIndex(pageIndex);
        int pageSize = resolvePageSize();
        Query<Datastream> query = sensorThingsService.datastreams().query();
        query.top(pageSize);
        query.skip(Math.multiplyExact(pageIndex, pageSize));

        try {
            EntityList<Datastream> entityList = query.list();
            return toList(entityList.iterator());
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to fetch FROST datastream page " + pageIndex, ex);
        }
    }

    public int countDatastreamPages() {
        return countPages(this::fetchDatastreamsPage);
    }

    public List<Thing> fetchThingsPage(int pageIndex) {
        validatePageIndex(pageIndex);
        int pageSize = resolvePageSize();
        Query<Thing> query = sensorThingsService.things().query();
        query.top(pageSize);
        query.skip(Math.multiplyExact(pageIndex, pageSize));

        try {
            EntityList<Thing> entityList = query.list();
            return toList(entityList.iterator());
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to fetch FROST thing page " + pageIndex, ex);
        }
    }

    public int countThingPages() {
        return countPages(this::fetchThingsPage);
    }

    public List<Sensor> fetchSensorsPage(int pageIndex) {
        validatePageIndex(pageIndex);
        int pageSize = resolvePageSize();
        Query<Sensor> query = sensorThingsService.sensors().query();
        query.top(pageSize);
        query.skip(Math.multiplyExact(pageIndex, pageSize));

        try {
            EntityList<Sensor> entityList = query.list();
            return toList(entityList.iterator());
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to fetch FROST sensor page " + pageIndex, ex);
        }
    }

    public int countSensorPages() {
        return countPages(this::fetchSensorsPage);
    }

    public List<ObservedProperty> fetchObservedPropertiesPage(int pageIndex) {
        validatePageIndex(pageIndex);
        int pageSize = resolvePageSize();
        Query<ObservedProperty> query = sensorThingsService.observedProperties().query();
        query.top(pageSize);
        query.skip(Math.multiplyExact(pageIndex, pageSize));

        try {
            EntityList<ObservedProperty> entityList = query.list();
            return toList(entityList.iterator());
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to fetch FROST observedProperty page " + pageIndex, ex);
        }
    }

    public int countObservedPropertyPages() {
        return countPages(this::fetchObservedPropertiesPage);
    }

    public List<FeatureOfInterest> fetchFeaturesOfInterestPage(int pageIndex) {
        validatePageIndex(pageIndex);
        int pageSize = resolvePageSize();
        Query<FeatureOfInterest> query = sensorThingsService.featuresOfInterest().query();
        query.top(pageSize);
        query.skip(Math.multiplyExact(pageIndex, pageSize));

        try {
            EntityList<FeatureOfInterest> entityList = query.list();
            return toList(entityList.iterator());
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to fetch FROST featureOfInterest page " + pageIndex, ex);
        }
    }

    public int countFeatureOfInterestPages() {
        return countPages(this::fetchFeaturesOfInterestPage);
    }

    private Query<Observation> baseObservationQuery(FrostProperties.DatastreamConfig datastreamConfig) {
        ObservationDao observationDao = sensorThingsService.observations();
        Query<Observation> query = observationDao.query();

        String filter = "Datastream/id eq " + datastreamConfig.getDatastreamId();
        String configuredFilter = StringUtils.hasText(datastreamConfig.getFilter())
                ? datastreamConfig.getFilter()
                : frostProperties.getFilter();
        if (StringUtils.hasText(configuredFilter)) {
            filter = "(" + filter + ") and (" + configuredFilter + ")";
        }

        query.filter(filter);

        if (StringUtils.hasText(frostProperties.getOrderBy())) {
            query.orderBy(frostProperties.getOrderBy());
        }
        if (StringUtils.hasText(frostProperties.getExpand())) {
            query.expand(frostProperties.getExpand());
        }
        return query;
    }

    private int resolvePageSize() {
        if (frostProperties.getPageSize() != null && frostProperties.getPageSize() > 0) {
            return frostProperties.getPageSize();
        }
        return 500;
    }

    private List<FrostProperties.DatastreamConfig> fetchDatastreamConfigsFromServer() {
        List<FrostProperties.DatastreamConfig> configs = new ArrayList<>();
        int pageIndex = 0;
        int skippedDatastreams = 0;
        long totalStart = System.nanoTime();

        while (true) {
            long pageStart = System.nanoTime();
            List<Datastream> page = fetchDatastreamsPage(pageIndex);
            long pageElapsedMs = (System.nanoTime() - pageStart) / 1_000_000;

            if (page.isEmpty()) {
                log.debug("Discovery: page {} empty after {} ms, stopping", pageIndex, pageElapsedMs);
                break;
            }

            log.debug("Discovery: fetched page {} with {} datastreams in {} ms", pageIndex, page.size(), pageElapsedMs);

            for (Datastream datastream : page) {
                if (shouldSkipDatastream(datastream)) {
                    skippedDatastreams++;
                    continue;
                }

                long convertStart = System.nanoTime();
                FrostProperties.DatastreamConfig config = toDatastreamConfig(datastream);
                long convertElapsedMs = (System.nanoTime() - convertStart) / 1_000_000;

                if (shouldSkipDatastreamConfig(config)) {
                    skippedDatastreams++;
                    continue;
                }

                log.debug("Discovery: datastream id={} converted to config in {} ms", config.getDatastreamId(), convertElapsedMs);
                configs.add(config);
            }

            if (!frostProperties.isFollowPaginationLinks() || page.size() < resolvePageSize()) {
                break;
            }
            pageIndex++;
            break; //TODO DA TOGLIERE
        }

        long totalElapsedMs = (System.nanoTime() - totalStart) / 1_000_000;
        log.info("Discovered {} datastream configurations from FROST server in {} ms (pages={}, skipped={})",
                configs.size(), totalElapsedMs, pageIndex + 1, skippedDatastreams);
        return configs;
    }

    private boolean shouldSkipDatastream(Datastream datastream) {
        if (datastream == null) {
            log.debug("Skipping null FROST datastream");
            return true;
        }

        Long datastreamId = idValue(datastream.getId());
        if (datastreamId == null) {
            log.debug("Skipping FROST datastream because id is missing");
            return true;
        }

        return false;
    }

    private boolean shouldSkipDatastreamConfig(FrostProperties.DatastreamConfig config) {
        // logica se devo skippare il DatastreamConfig
        if (config == null) {
            log.debug("Skipping null FROST datastream");
            return true;
        }

        return config == null;
    }

    private FrostProperties.DatastreamConfig toDatastreamConfig(Datastream datastream) {
        Long datastreamId = idValue(datastream != null ? datastream.getId() : null);
        if (datastreamId == null) {
            log.warn("Skipping FROST datastream without id");
            return null;
        }

        Sensor sensor;
        try {
            sensor = datastream.getSensor();
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to resolve FROST sensor for datastream " + datastreamId, ex);
        }

        Long sensorId = idValue(sensor != null ? sensor.getId() : null);
        if (sensorId == null) {
            log.warn("Skipping FROST datastream {} because sensor id is missing", datastreamId);
            return null;
        }

        FrostProperties.DatastreamConfig config = new FrostProperties.DatastreamConfig();
        config.setDatastreamId(datastreamId);
        config.setSensorId(sensorId);
        config.setDescription(datastream.getDescription());
        if (StringUtils.hasText(frostProperties.getFilter())) {
            config.setFilter(frostProperties.getFilter());
        }

        try {
            ObservedProperty observedProperty = datastream.getObservedProperty();
            if (observedProperty != null) {
                config.setObservedProperty(observedProperty.getName());
            }
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to resolve FROST observedProperty for datastream " + datastreamId, ex);
        }

        return config;
    }

    private Long idValue(Id id) {
        if (id == null || id.getValue() == null) {
            return null;
        }
        Object value = id.getValue();
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }

    private <T> List<T> toList(Iterator<T> iterator) {
        List<T> observations = new ArrayList<>();
        while (iterator.hasNext()) {
            observations.add(iterator.next());
        }
        return observations;
    }

    private void validatePageIndex(int pageIndex) {
        if (pageIndex < 0) {
            throw new IllegalArgumentException("pageIndex must be >= 0");
        }
    }

    private int countPages(IntFunction<? extends List<?>> pageFetcher) {
        int pageSize = resolvePageSize();
        int pageIndex = 0;

        while (true) {
            List<?> page = pageFetcher.apply(pageIndex);
            if (page.isEmpty()) {
                return pageIndex;
            }
            if (page.size() < pageSize) {
                return pageIndex + 1;
            }
            pageIndex++;
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

