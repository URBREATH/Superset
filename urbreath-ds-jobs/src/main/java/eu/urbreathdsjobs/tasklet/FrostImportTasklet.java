package eu.urbreathdsjobs.tasklet;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.TimeObject;
import eu.urbreathdsjobs.client.frost.FrostClientService;
import eu.urbreathdsjobs.client.frost.FrostProperties;
import eu.urbreathdsjobs.common.MeasurementAttributeEnum;
import eu.urbreathdsjobs.common.SensorAttributeEnum;
import eu.urbreathdsjobs.dao.SensorDao;
import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.model.Sensor;
import eu.urbreathdsjobs.service.MeasurementPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.statistics.descriptive.Max;
import org.apache.commons.statistics.descriptive.Mean;
import org.apache.commons.statistics.descriptive.Median;
import org.apache.commons.statistics.descriptive.Min;
import org.apache.commons.statistics.descriptive.Quantile;
import org.apache.commons.statistics.descriptive.StandardDeviation;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
public class FrostImportTasklet implements Tasklet {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);
    private static final double[] QUANTILE_PROBABILITIES = {0.02, 0.24, 0.75, 0.98};
    private static final String MEASURE_MODE_PEOPLE = "people";
    private static final String MEASURE_MODE_STANDARD = "standard";

    private final FrostClientService frostClientService;
    private final FrostProperties frostProperties;
    private final SensorDao sensorDao;
    private final MeasurementPersistenceService measurementPersistenceService;

    @Value("${frost.dry-run:${frost.dry-run-config:false}}")
    private boolean dryRun;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {

        // 1. Recupera tutti i sensori con SENSOR_ID_EXTERNAL valorizzato
        List<Sensor> sensors = sensorDao.findSensorsWithExternalId();

        // Nel caso volessi estrarre uno specifico Sensore (test/debug/altro)
//        Optional<Sensor> sensor = sensorDao.findSensorByExternalId("1");
//        sensor.ifPresent(s -> {
//            int sensorResult = processSensor(s);
//        });

        log.info("Found {} sensors with SENSOR_ID_EXTERNAL in metadata", sensors.size());

        int successCount = 0;
        int failedCount = 0;
        long totalMeasurements = 0;
        int peopleSensors = 0;
        int standardSensors = 0;

        // 2. Un sensore alla volta: la transazione è sul singolo sensore
        for (Sensor sensor : sensors) {
            try {
                if (isPeopleSensor(sensor)) {
                    peopleSensors++;
                } else {
                    standardSensors++;
                }
                int sensorResult = processSensor(sensor);
                totalMeasurements += sensorResult;
                successCount++;
            } catch (Exception ex) {
                failedCount++;
                log.warn("Failed to process sensorId={}: {}", sensor.getIdSensor(), ex.getMessage(), ex);
            }
        }

        log.info("FrostImportTasklet completed: sensors={}, success={}, failed={}, peopleSensors={}, standardSensors={}, totalMeasurements={}, dryRun={}",
                sensors.size(), successCount, failedCount, peopleSensors, standardSensors, totalMeasurements, dryRun);

        return RepeatStatus.FINISHED;
    }

    /**
     * Elabora un singolo sensore:
     * <ol>
     *   <li>Costruisce il {@link FrostProperties.DatastreamConfig} dal metadata</li>
     *   <li>Scarica tutte le osservazioni FROST paginate</li>
     *   <li>Raggruppa le osservazioni per giorno</li>
     *   <li>TODO: per ogni giorno calcola la sintesi giornaliera e la inserisce in {@code measurement}</li>
     * </ol>
     *
     * @return numero totale di osservazioni recuperate
     */
    private int processSensor(Sensor sensor) {
        validateSensor(sensor);

        FrostProperties.DatastreamConfig config = buildDatastreamConfig(sensor);
        if (config == null) {
            log.warn("Cannot build DatastreamConfig for sensorId={}, SENSOR_ID_EXTERNAL not valid – skipping",
                    sensor.getIdSensor());
            return 0;
        }

        String measureMode = resolveMeasureMode(sensor);
        log.info("Processing sensorId={}, displayName={}, datastreamId={}, measureMode={}, lastObservationDate={}",
                sensor.getIdSensor(),
                sensor.getDisplayName(),
                config.getDatastreamId(),
                measureMode,
                sensor.getMetadataAttributeAsString(SensorAttributeEnum.LAST_OBSERVATION_DATE));

        // Scarica tutte le osservazioni per questo datastream
        List<Observation> observations = fetchAllObservations(config);

        if (observations.isEmpty()) {
            log.info("No observations found for sensorId={}, datastreamId={}", sensor.getIdSensor(), config.getDatastreamId());
            return 0;
        }

        // Raggruppa per giorno (ordinate per data grazie a TreeMap)
        Map<LocalDate, List<Observation>> byDay = groupObservationsByDay(observations);

        log.info("sensorId={}, datastreamId={}, measureMode={}: fetched {} observations across {} days",
                sensor.getIdSensor(),
                config.getDatastreamId(),
                measureMode,
                observations.size(),
                byDay.size());

        List<Measurement> measurements = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Observation>> entry : byDay.entrySet()) {
            LocalDate day = entry.getKey();
            List<Observation> dayObservations = entry.getValue();

            Measurement measurement = buildDailyMeasurement(sensor, day, dayObservations);
            if (measurement == null) {
                continue;
            }

            measurements.add(measurement);
            log.debug("  sensorId={} | day={} | measureMode={} | observations={} | min={} | max={} | avg={} | val={} | sd={}",
                    sensor.getIdSensor(),
                    day,
                    measureMode,
                    dayObservations.size(),
                    measurement.getMin(),
                    measurement.getMax(),
                    measurement.getAvg(),
                    measurement.getVal(),
                    measurement.getSd());
        }

        Instant latestObservationInstant = resolveLatestObservationInstant(observations);
        persistMeasurements(sensor, measurements, latestObservationInstant);
        return measurements.size();
    }

    private static final String DISPLAY_NAME_PEOPLE = "people";

    private Measurement buildDailyMeasurement(Sensor sensor, LocalDate day, List<Observation> dayObservations) {
        double[] values = extractNumericResults(dayObservations);
        if (values.length == 0) {
            log.warn("Skipping day {} for sensorId={} because no numeric observations were found", day, sensor.getIdSensor());
            return null;
        }

        Measurement measurement = new Measurement();
        LocalDateTime dateFrom = LocalDateTime.of(day, LocalTime.MIDNIGHT);
        LocalDateTime dateTo = LocalDateTime.of(day, LocalTime.MAX);

        measurement.setIdMeasure(SNOWFLAKE.nextId());
        measurement.setIdParam(sensor.getIdParam());
        measurement.setIdSensor(sensor.getIdSensor());
        measurement.setPeriod("1day");
        measurement.setDateFrom(dateFrom);
        measurement.setDateTo(dateTo);

        if (isPeopleSensor(sensor)) {
            // Sensore "people": conta i passaggi giornalieri → val = somma, sd opzionale
            double sum = 0.0;
            StandardDeviation sd = StandardDeviation.create();
            for (double v : values) {
                sum += v;
                sd.accept(v);
            }
            measurement.setVal(sum);
            measurement.setSd(values.length > 1 ? sd.getAsDouble() : null);

            log.debug("  [people] sensorId={} | day={} | count={} | sum={}",
                    sensor.getIdSensor(), day, values.length, sum);
        } else {
            // Sensore standard: calcolo statistiche complete
            Min min = Min.create();
            Max max = Max.create();
            Mean mean = Mean.create();
            StandardDeviation sd = StandardDeviation.create();
            for (double v : values) {
                min.accept(v);
                max.accept(v);
                mean.accept(v);
                sd.accept(v);
            }

            double median = Median.withDefaults().evaluate(values);
            double[] quantiles = Quantile.withDefaults().evaluate(values, QUANTILE_PROBABILITIES);

            measurement.setMin(min.getAsDouble());
            measurement.setMax(max.getAsDouble());
            measurement.setAvg(mean.getAsDouble());
            measurement.setVal(mean.getAsDouble());
            measurement.setSd(sd.getAsDouble());
            measurement.setMedian(median);
            measurement.setQ02(quantiles[0]);
            measurement.setQ24(quantiles[1]);
            measurement.setQ75(quantiles[2]);
            measurement.setQ98(quantiles[3]);
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put(MeasurementAttributeEnum.MEASURE_TYPE.name(), "FROST_SERVER");
        measurement.setMetadata(metadata);

        return measurement;
    }

    /**
     * Restituisce {@code true} se il sensore rappresenta un contatore di persone
     * (displayName = "people", case-insensitive).
     */
    private boolean isPeopleSensor(Sensor sensor) {
        return DISPLAY_NAME_PEOPLE.equalsIgnoreCase(sensor.getDisplayName());
    }

    private String resolveMeasureMode(Sensor sensor) {
        return isPeopleSensor(sensor) ? MEASURE_MODE_PEOPLE : MEASURE_MODE_STANDARD;
    }

    private double[] extractNumericResults(List<Observation> dayObservations) {
        List<Double> values = new ArrayList<>();
        for (Observation observation : dayObservations) {
            Double value = toDouble(observation != null ? observation.getResult() : null);
            if (value != null) {
                values.add(value);
            }
        }
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private Double toDouble(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof Number number) {
            return number.doubleValue();
        }
        if (result instanceof String value && StringUtils.hasText(value)) {
            try {
                return Double.valueOf(value.trim());
            } catch (NumberFormatException ex) {
                log.debug("Skipping non-numeric observation result='{}'", value);
                return null;
            }
        }
        log.debug("Skipping unsupported observation result type={}", result.getClass().getName());
        return null;
    }

    private void persistMeasurements(Sensor sensor, List<Measurement> measurements, Instant latestObservationInstant) {
        if (measurements.isEmpty()) {
            return;
        }

        if (latestObservationInstant == null) {
            throw new IllegalStateException("Unable to resolve latest observation timestamp for sensor " + sensor.getIdSensor());
        }

        String lastObservationDate = DateTimeFormatter.ISO_INSTANT.format(latestObservationInstant);
        String measureMode = resolveMeasureMode(sensor);

        if (dryRun) {
            log.info("[DRY-RUN] Would insert {} measurements for sensorId={} (mode={}) and update LAST_OBSERVATION_DATE={}",
                    measurements.size(), sensor.getIdSensor(), measureMode, lastObservationDate);
            for (Measurement measurement : measurements) {
                log.info(
                        "[DRY-RUN] Measurement idMeasure={}, idSensor={}, idParam={}, period={}, dateFrom={}, dateTo={}, min={}, q02={}, q24={}, median={}, q75={}, q98={}, max={}, avg={}, sd={}, val={}, mode={}, metadata={}",
                        measurement.getIdMeasure(),
                        measurement.getIdSensor(),
                        measurement.getIdParam(),
                        measurement.getPeriod(),
                        measurement.getDateFrom(),
                        measurement.getDateTo(),
                        measurement.getMin(),
                        measurement.getQ02(),
                        measurement.getQ24(),
                        measurement.getMedian(),
                        measurement.getQ75(),
                        measurement.getQ98(),
                        measurement.getMax(),
                        measurement.getAvg(),
                        measurement.getSd(),
                        measurement.getVal(),
                        measureMode,
                        measurement.getMetadata()
                );
            }
            return;
        }

        measurementPersistenceService.writeMeasurementsAndUpdateLastObservationDate(
                sensor.getIdSensor(),
                lastObservationDate,
                measurements
        );
        log.info("Inserted {} daily measurements for sensorId={} (mode={}) and updated LAST_OBSERVATION_DATE={}",
                measurements.size(), sensor.getIdSensor(), measureMode, lastObservationDate);
    }

    private Instant resolveLatestObservationInstant(List<Observation> observations) {
        Instant latest = null;
        for (Observation observation : observations) {
            Instant current = resolveObservationInstant(observation);
            if (current != null && (latest == null || current.isAfter(latest))) {
                latest = current;
            }
        }
        return latest;
    }

    private Instant resolveObservationInstant(Observation observation) {
        if (observation == null) {
            return null;
        }

        TimeObject phenomenonTime = observation.getPhenomenonTime();
        if (phenomenonTime != null) {
            try {
                if (phenomenonTime.isInterval()) {
                    return phenomenonTime.getAsInterval().getEnd();
                }
                return phenomenonTime.getAsDateTime().toInstant();
            } catch (Exception ex) {
                log.debug("Cannot resolve phenomenonTime instant for observation id={}: {}", observation.getId(), ex.getMessage());
            }
        }

        return observation.getResultTime() != null ? observation.getResultTime().toInstant() : null;
    }

    private void validateSensor(Sensor sensor) {
        if (sensor == null) {
            throw new IllegalArgumentException("sensor must not be null");
        }
        if (sensor.getIdSensor() == null) {
            throw new IllegalArgumentException("sensor.idSensor must not be null");
        }
        if (sensor.getIdParam() == null) {
            throw new IllegalArgumentException("sensor.idParam must not be null for sensor " + sensor.getIdSensor());
        }
    }

    /**
     * Raggruppa le osservazioni per giorno di calendario (UTC).
     * Le osservazioni senza {@code phenomenonTime} valido vengono scartate con un warning.
     * Il {@link TreeMap} garantisce l'ordine cronologico delle chiavi.
     */
    private Map<LocalDate, List<Observation>> groupObservationsByDay(List<Observation> observations) {
        Map<LocalDate, List<Observation>> byDay = new TreeMap<>();

        for (Observation obs : observations) {
            LocalDate day = extractLocalDate(obs);
            if (day == null) {
                log.debug("Skipping observation id={}: phenomenonTime is null or unparseable", obs.getId());
                continue;
            }
            byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(obs);
        }

        return byDay;
    }

    /**
     * Estrae la {@link LocalDate} (UTC) dal {@code phenomenonTime} dell'osservazione.
     * Gestisce sia il caso {@link de.fraunhofer.iosb.ilt.sta.model.TimeObject} che rappresenta
     * un istante ({@code ZonedDateTime}) sia un intervallo ({@code Interval}).
     *
     * @return la data o {@code null} se non disponibile
     */
    private LocalDate extractLocalDate(Observation obs) {
        TimeObject phenomenonTime = obs.getPhenomenonTime();
        if (phenomenonTime == null) {
            return null;
        }
        try {
            if (phenomenonTime.isInterval()) {
                // Intervallo: usa il momento di inizio
                return phenomenonTime.getAsInterval()
                        .getStart()
                        .atOffset(ZoneOffset.UTC)
                        .toLocalDate();
            } else {
                return phenomenonTime.getAsDateTime()
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .toLocalDate();
            }
        } catch (Exception ex) {
            log.debug("Cannot extract date from phenomenonTime for observation id={}: {}", obs.getId(), ex.getMessage());
            return null;
        }
    }

    /**
     * Costruisce un {@link FrostProperties.DatastreamConfig} a partire dal metadata del sensore.
     * <ul>
     *   <li>{@code SENSOR_ID_EXTERNAL}: ID del Datastream FROST (obbligatorio)</li>
     *   <li>{@code LAST_OBSERVATION_DATE}: se presente, aggiunge un filtro
     *       {@code phenomenonTime gt <data>} per importare solo le osservazioni successive</li>
     * </ul>
     */
    private FrostProperties.DatastreamConfig buildDatastreamConfig(Sensor sensor) {
        String externalIdStr = sensor.getMetadataAttributeAsString(SensorAttributeEnum.SENSOR_ID_EXTERNAL);
        if (!StringUtils.hasText(externalIdStr)) {
            return null;
        }

        Long datastreamId;
        try {
            datastreamId = Long.parseLong(externalIdStr.trim());
        } catch (NumberFormatException ex) {
            log.warn("Invalid SENSOR_ID_EXTERNAL='{}' for sensorId={} – expected a numeric value",
                    externalIdStr, sensor.getIdSensor());
            return null;
        }

        FrostProperties.DatastreamConfig config = new FrostProperties.DatastreamConfig();
        config.setDatastreamId(datastreamId);
        config.setSensorId(sensor.getIdSensor());

        // Compone il filtro: se LAST_OBSERVATION_DATE è presente filtra solo le nuove osservazioni
        String lastObservationDate = sensor.getMetadataAttributeAsString(SensorAttributeEnum.LAST_OBSERVATION_DATE);
        if (StringUtils.hasText(lastObservationDate)) {
            // Sintassi OData STA: phenomenonTime gt 2024-01-01T00:00:00Z
            String dateFilter = "phenomenonTime gt " + lastObservationDate.trim();
            String globalFilter = frostProperties.getFilter();
            if (StringUtils.hasText(globalFilter)) {
                config.setFilter("(" + dateFilter + ") and (" + globalFilter + ")");
            } else {
                config.setFilter(dateFilter);
            }
        }
        // Se LAST_OBSERVATION_DATE non è presente, baseObservationQuery userà il filtro globale

        return config;
    }

    /**
     * Recupera tutte le osservazioni per un dato {@link FrostProperties.DatastreamConfig}
     * tramite paginazione.
     */
    private List<Observation> fetchAllObservations(FrostProperties.DatastreamConfig config) {
        List<Observation> all = new ArrayList<>();
        int pageIndex = 0;

        int effectivePageSize = (frostProperties.getPageSize() != null && frostProperties.getPageSize() > 0)
                ? frostProperties.getPageSize()
                : 500;

        while (true) {
            List<Observation> page = frostClientService.fetchObservationsPage(config, pageIndex);

            if (page.isEmpty()) {
                log.debug("No more observations at page {} for datastreamId={}", pageIndex, config.getDatastreamId());
                break;
            }

            all.addAll(page);

            log.debug("Page {} for datastreamId={}: {} observations (running total={})",
                    pageIndex, config.getDatastreamId(), page.size(), all.size());

            if (!frostProperties.isFollowPaginationLinks() || page.size() < effectivePageSize) {
                break;
            }

            pageIndex++;
        }

        return all;
    }
}
