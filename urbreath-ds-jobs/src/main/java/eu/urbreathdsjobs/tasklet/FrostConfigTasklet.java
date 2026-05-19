package eu.urbreathdsjobs.tasklet;

import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import eu.urbreathdsjobs.client.frost.FrostClientService;
import eu.urbreathdsjobs.common.Constants;
import eu.urbreathdsjobs.common.SensorAttributeEnum;
import eu.urbreathdsjobs.model.City;
import eu.urbreathdsjobs.model.Location;
import eu.urbreathdsjobs.model.Parameter;
import eu.urbreathdsjobs.dao.SensorDao;
import eu.urbreathdsjobs.service.FrostSensorPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FrostConfigTasklet implements Tasklet {

    private final FrostClientService frostClientService;
    private final SensorDao sensorDao;
    private final FrostSensorPersistenceService frostSensorPersistenceService;

    @Value("${frost.dry-run-config:false}")
    private boolean dryRun;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        int totalPages = frostClientService.countSensorPages();
        int totalDatastreamsProcessed = 0;
        int totalDatastreamsSkipped = 0;
        int totalDatastreamsInserted = 0;

        for (int page = 0; page < totalPages; page++) {
            List<Sensor> sensors = frostClientService.fetchSensorsPage(page);
            log.debug("Fetched sensor page {}/{}: {} sensors", page + 1, totalPages, sensors.size());

            for (Sensor sensor : sensors) {
                Long sensorId = frostClientService.idValue(sensor.getId());
                if (sensorId == null) {
                    log.warn("Skipping sensor because id is missing");
                    continue;
                }

                List<Datastream> datastreams = frostClientService.fetchDatastreamsForSensor(sensorId);
                log.debug("Processing {} datastreams for sensorId={}", datastreams.size(), sensorId);

                for (Datastream datastream : datastreams) {
                    totalDatastreamsProcessed++;
                    
                    if (!processDatastream(datastream)) {
                        totalDatastreamsSkipped++;
                    } else {
                        totalDatastreamsInserted++;
                    }
                }
            }
        }

        log.info("FROST config job completed: processed={}, inserted={}, skipped={}",
                totalDatastreamsProcessed, totalDatastreamsInserted, totalDatastreamsSkipped);
        return RepeatStatus.FINISHED;
    }

    private boolean processDatastream(Datastream datastream) {
        Id id = datastream.getId();
        Long idSensor = frostClientService.idValue(id);

        String unitSymbol = extractUnitSymbol(datastream);

        // 1. Valida unitSymbol
        if (!Constants.ACCEPTED_UNIT_SYMBOLS.contains(unitSymbol)) {
            log.debug("Skipping datastream id={} - unitSymbol='{}' not in accepted list", id, unitSymbol);
            return false;
        }

        // 2. Valida sensore non esiste già
        if (sensorDao.sensorExists(String.valueOf(idSensor))) {
            log.debug("Skipping datastream id={} - sensor with SENSOR_ID_EXTERNAL={} already exists", id, idSensor);
            return false;
        }

        // 3. Elabora e inserisci
        Parameter parameter = getParameter(datastream);
        Location location = getLocation(datastream);
        City city = getCity(datastream);
        eu.urbreathdsjobs.model.Sensor urSensor = getSensor(idSensor, parameter, location);

        if (dryRun) {
            log.info("[DRY-RUN] Would insert: Parameter(name={}, units={}, displayName={}), Location(lat={}, lon={}), City(name={}), Sensor(name={}, displayName={}, metadata.SENSOR_ID_EXTERNAL={})",
                parameter.getName(), parameter.getUnits(), parameter.getDisplayName(),
                location.getLatitude(), location.getLongitude(),
                city != null ? city.getName() : "null",
                urSensor.getName(), urSensor.getDisplayName(), idSensor);
        } else {
            if (city == null) {
                log.warn("City is null for datastream id={}, using fallback 'n/a' will be applied during insert", id);
            }
            frostSensorPersistenceService.insertSensor(parameter, location, urSensor, city);
            log.info("Successfully inserted datastream id={} - Sensor(name={}, idParam={}, idLocation={})",
                id, urSensor.getName(), urSensor.getIdParam(), urSensor.getIdLocation());
        }
        
        return true;
    }

    private String extractUnitSymbol(Datastream datastream) {
        if (datastream.getUnitOfMeasurement() != null) {
            return datastream.getUnitOfMeasurement().getSymbol();
        }
        return "N/A";
    }

    private Location getLocation(Datastream datastream) {
        Location location = new Location();

        GeoJsonObject observedArea = datastream.getObservedArea();
        if (observedArea instanceof Point point) {
            LngLatAlt coordinates = point.getCoordinates();
            if (coordinates != null) {
                // GeoJSON coordinates order: [longitude, latitude]
                location.setLongitude(coordinates.getLongitude());
                location.setLatitude(coordinates.getLatitude());
            }

        }

        return location;
    }

    private Parameter getParameter(Datastream datastream) {
        String description = datastream.getDescription() != null
            ? datastream.getName()
            : "N/A";
        String unitName = datastream.getUnitOfMeasurement() != null
            ? datastream.getUnitOfMeasurement().getName()
            : "N/A";

        String nameFromDescription = description.split("\\s+")[0];

        Parameter parameter = new Parameter();
        parameter.setName(nameFromDescription);
        parameter.setUnits(datastream.getUnitOfMeasurement().getSymbol());
        parameter.setDisplayName(unitName);
        parameter.setDescription(description);

        return parameter;
    }

    private City getCity(Datastream datastream) {
        Object properties = datastream.getProperties();
        if (!(properties instanceof Map<?, ?> propertiesMap)) {
            return null;
        }

        Object pilot = propertiesMap.get("pilot");
        if (pilot == null) {
            return null;
        }

        String cityName = pilot.toString().trim();
        if (cityName.isEmpty()) {
            return null;
        }

        City city = new City();
        city.setName(cityName);
        return city;
    }

    private eu.urbreathdsjobs.model.Sensor getSensor(Long idSensor, Parameter parameter, Location location) {
        eu.urbreathdsjobs.model.Sensor urSensor = new eu.urbreathdsjobs.model.Sensor();

        urSensor.setLatitude(location.getLatitude());
        urSensor.setLongitude(location.getLongitude());
        urSensor.setName(parameter.getName());
        urSensor.setDisplayName(parameter.getDisplayName());
        urSensor.setMetadataAttribute(SensorAttributeEnum.SENSOR_ID_EXTERNAL, String.valueOf(idSensor));

        return urSensor;
    }


}
