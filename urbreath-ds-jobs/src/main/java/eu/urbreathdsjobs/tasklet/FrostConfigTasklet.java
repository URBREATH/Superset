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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class FrostConfigTasklet implements Tasklet {

    private static final String UNIT_SYMBOL_PEOPLE = "ppl";
    private static final Set<String> EXPECTED_PEOPLE_NAMES = Set.of(
            "PeopleCountTotal",
            "PeopleCountIn",
            "PeopleCountOut"
    );

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

    /**
     * Recovery mirato per sensori "people" (unitSymbol=ppl):
     * riallinea SENSOR_ID_EXTERNAL sui sensori già presenti ma con metadata mancante.
     *
     * @return numero di sensori recuperati
     */
    public int recoverMissingExternalSensorIdsForPeopleDatastreams() {
        int recovered = 0;
        int scannedPeopleDatastreams = 0;
        int skippedAlreadyMapped = 0;
        int skippedUnexpectedPeopleName = 0;
        Map<String, RecoveryGroup> groups = new LinkedHashMap<>();
        int totalPages = frostClientService.countSensorPages();

        for (int page = 0; page < totalPages; page++) {
            List<Sensor> sensors = frostClientService.fetchSensorsPage(page);
            for (Sensor sensor : sensors) {
                Long sensorId = frostClientService.idValue(sensor.getId());
                if (sensorId == null) {
                    continue;
                }

                List<Datastream> datastreams = frostClientService.fetchDatastreamsForSensor(sensorId);
                for (Datastream datastream : datastreams) {
                    if (!UNIT_SYMBOL_PEOPLE.equals(extractUnitSymbol(datastream))) {
                        continue;
                    }
                    scannedPeopleDatastreams++;

                    Long externalSensorId = frostClientService.idValue(datastream.getId());
                    if (externalSensorId == null) {
                        continue;
                    }

                    // Se già presente in DB non è un caso da recuperare.
                    if (sensorDao.sensorExists(String.valueOf(externalSensorId))) {
                        skippedAlreadyMapped++;
                        continue;
                    }

                    Parameter parameter = getParameter(datastream);
                    Location location = getLocation(datastream);
                    eu.urbreathdsjobs.model.Sensor candidate = getSensor(externalSensorId, parameter, location);
                    if (!isExpectedPeopleName(candidate.getName())) {
                        skippedUnexpectedPeopleName++;
                        log.warn("Skipping people recovery for unexpected datastream name='{}' (externalId={}, lat={}, lon={})",
                                candidate.getName(), externalSensorId, candidate.getLatitude(), candidate.getLongitude());
                        continue;
                    }

                    String key = recoveryKey(candidate);
                    RecoveryGroup group = groups.computeIfAbsent(key, k -> new RecoveryGroup(candidate));
                    group.externalIds.add(externalSensorId);
                }
            }
        }

        for (RecoveryGroup group : groups.values()) {
            List<Long> distinctExternalIds = group.externalIds.stream()
                    .distinct()
                    .sorted()
                    .toList();

            List<Long> candidates = sensorDao.findSensorIdsMissingExternalByFingerprint(
                    group.template.getName(),
                    group.template.getDisplayName(),
                    group.template.getLatitude(),
                    group.template.getLongitude()
            );

            if (candidates.isEmpty()) {
                log.warn("Recovery group without DB candidates for name={}, displayName={}, lat={}, lon={}, externalIds={}",
                        group.template.getName(),
                        group.template.getDisplayName(),
                        group.template.getLatitude(),
                        group.template.getLongitude(),
                        distinctExternalIds);
                continue;
            }

            if (candidates.size() != distinctExternalIds.size()) {
                log.warn("Ambiguous recovery group (cardinality mismatch) for name={}, displayName={}, lat={}, lon={}: externalIds={}, candidateSensors={}",
                        group.template.getName(),
                        group.template.getDisplayName(),
                        group.template.getLatitude(),
                        group.template.getLongitude(),
                        distinctExternalIds.size(),
                        candidates.size());
                log.warn("Details: externalIds={}, candidateSensorIds={}", distinctExternalIds, candidates);
                continue;
            }

            List<Long> sortedCandidateIds = candidates.stream().sorted().collect(Collectors.toCollection(ArrayList::new));
            List<Long> sortedExternalIds = distinctExternalIds.stream().sorted().collect(Collectors.toCollection(ArrayList::new));

            for (int i = 0; i < sortedExternalIds.size(); i++) {
                Long externalId = sortedExternalIds.get(i);
                Long sensorId = sortedCandidateIds.get(i);
                if (dryRun) {
                    log.info("[DRY-RUN] Would recover SENSOR_ID_EXTERNAL={} on existing sensor id_sensor={} (name={}, displayName={})",
                            externalId, sensorId, group.template.getName(), group.template.getDisplayName());
                } else {
                    sensorDao.updateSensorExternalId(sensorId, String.valueOf(externalId));
                    log.info("Recovered SENSOR_ID_EXTERNAL={} on existing sensor id_sensor={} (name={}, displayName={})",
                            externalId, sensorId, group.template.getName(), group.template.getDisplayName());
                }
                recovered++;
            }
        }

        log.info("FROST people recovery completed: scannedPeopleDatastreams={}, groups={}, skippedAlreadyMapped={}, skippedUnexpectedPeopleName={}, recovered={}, dryRun={}",
                scannedPeopleDatastreams, groups.size(), skippedAlreadyMapped, skippedUnexpectedPeopleName, recovered, dryRun);
        return recovered;
    }

    private boolean isExpectedPeopleName(String name) {
        return name != null && EXPECTED_PEOPLE_NAMES.contains(name);
    }

    private String recoveryKey(eu.urbreathdsjobs.model.Sensor sensor) {
        String name = sensor.getName() == null ? "" : sensor.getName();
        String displayName = sensor.getDisplayName() == null ? "" : sensor.getDisplayName();
        String lat = sensor.getLatitude() == null ? "" : String.format(java.util.Locale.ROOT, "%.6f", sensor.getLatitude());
        String lon = sensor.getLongitude() == null ? "" : String.format(java.util.Locale.ROOT, "%.6f", sensor.getLongitude());
        return name + "|" + displayName + "|" + lat + "|" + lon;
    }

    private static final class RecoveryGroup {
        private final eu.urbreathdsjobs.model.Sensor template;
        private final List<Long> externalIds = new ArrayList<>();

        private RecoveryGroup(eu.urbreathdsjobs.model.Sensor template) {
            this.template = template;
        }
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

        if (tryRecoverMissingExternalId(datastream, idSensor)) {
            log.info("Recovered SENSOR_ID_EXTERNAL={} for existing sensor using datastream id={}", idSensor, id);
            return true;
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
            try {
                frostSensorPersistenceService.insertSensor(parameter, location, urSensor, city);
                log.info("Successfully inserted datastream id={} - Sensor(name={}, idParam={}, idLocation={})",
                    id, urSensor.getName(), urSensor.getIdParam(), urSensor.getIdLocation());
            } catch (Exception ex) {
                log.error("FAILED to insert datastream id={} - Sensor(name={}, displayName={}): {}",
                    id, urSensor.getName(), urSensor.getDisplayName(), ex.getMessage(), ex);
                return false;
            }
        }
        
        return true;
    }

    private boolean tryRecoverMissingExternalId(Datastream datastream, Long externalSensorId) {
        if (externalSensorId == null) {
            return false;
        }
        if (sensorDao.sensorExists(String.valueOf(externalSensorId))) {
            return false;
        }

        Parameter parameter = getParameter(datastream);
        Location location = getLocation(datastream);
        eu.urbreathdsjobs.model.Sensor candidate = getSensor(externalSensorId, parameter, location);

        if (UNIT_SYMBOL_PEOPLE.equals(extractUnitSymbol(datastream)) && !isExpectedPeopleName(candidate.getName())) {
            log.warn("Skipping inline people recovery for unexpected datastream name='{}' (externalId={}, lat={}, lon={})",
                    candidate.getName(), externalSensorId, candidate.getLatitude(), candidate.getLongitude());
            return false;
        }

        List<Long> candidates = sensorDao.findSensorIdsMissingExternalByFingerprint(
                candidate.getName(),
                candidate.getDisplayName(),
                candidate.getLatitude(),
                candidate.getLongitude()
        );

        if (candidates.isEmpty()) {
            return false;
        }
        if (candidates.size() > 1) {
            log.warn("Ambiguous recovery for SENSOR_ID_EXTERNAL={}: {} candidate sensors found for name={}, displayName={}, lat={}, lon={}",
                    externalSensorId,
                    candidates.size(),
                    candidate.getName(),
                    candidate.getDisplayName(),
                    candidate.getLatitude(),
                    candidate.getLongitude());
            return false;
        }

        Long matchedSensorId = candidates.getFirst();
        if (dryRun) {
            log.info("[DRY-RUN] Would recover SENSOR_ID_EXTERNAL={} on existing sensor id_sensor={}",
                    externalSensorId, matchedSensorId);
            return true;
        }

        sensorDao.updateSensorExternalId(matchedSensorId, String.valueOf(externalSensorId));
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
