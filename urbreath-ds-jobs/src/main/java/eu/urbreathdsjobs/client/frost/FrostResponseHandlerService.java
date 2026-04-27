package eu.urbreathdsjobs.client.frost;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.TimeObject;
import eu.urbreathdsjobs.common.Constants;
import eu.urbreathdsjobs.common.MeasurementAttributeEnum;
import eu.urbreathdsjobs.model.Measurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.threeten.extra.Interval;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
public class FrostResponseHandlerService {

    private static final String FROST_OBSERVATION_ID = "FROST_OBSERVATION_ID";
    private static final String FROST_DATASTREAM_ID = "FROST_DATASTREAM_ID";
    private static final String FROST_DATASTREAM_NAME = "FROST_DATASTREAM_NAME";
    private static final String FROST_THING_ID = "FROST_THING_ID";
    private static final String FROST_THING_NAME = "FROST_THING_NAME";
    private static final String FROST_SENSOR_ID = "FROST_SENSOR_ID";
    private static final String FROST_SENSOR_NAME = "FROST_SENSOR_NAME";
    private static final String FROST_OBSERVED_PROPERTY_ID = "FROST_OBSERVED_PROPERTY_ID";
    private static final String FROST_OBSERVED_PROPERTY_NAME = "FROST_OBSERVED_PROPERTY_NAME";
    private static final String FROST_RESULT_TIME = "FROST_RESULT_TIME";
    private static final String FROST_PARAMETERS = "FROST_PARAMETERS";
    private static final String FROST_CITY = "FROST_CITY";
    private static final String FROST_CONFIGURED_OBSERVED_PROPERTY = "FROST_CONFIGURED_OBSERVED_PROPERTY";
    private static final String FROST_DATASTREAM_DESCRIPTION = "FROST_DATASTREAM_DESCRIPTION";

    private final FrostProperties frostProperties;

    public List<Measurement> handle(FrostProperties.DatastreamConfig datastreamConfig, List<Observation> observations) {
        List<Measurement> measurements = new ArrayList<>();
        if (observations == null) {
            return measurements;
        }

        for (Observation observation : observations) {
            measurements.add(mapObservation(datastreamConfig, observation));
        }

        return measurements;
    }

    public Measurement mapObservation(FrostProperties.DatastreamConfig datastreamConfig, Observation observation) {
        if (frostProperties.getIdParam() == null) {
            throw new IllegalStateException("Missing frost.id-param configuration");
        }
        if (datastreamConfig == null) {
            throw new IllegalArgumentException("Datastream configuration must not be null");
        }
        if (observation == null) {
            throw new IllegalArgumentException("Observation must not be null");
        }

        Datastream datastream = getDatastream(observation);
        Thing thing = getThing(datastream);
        Sensor sensor = getSensor(datastream);
        ObservedProperty observedProperty = getObservedProperty(datastream);

        Measurement measurement = new Measurement();
        measurement.setIdParam(frostProperties.getIdParam());
        measurement.setIdSensor(datastreamConfig.getSensorId());
        measurement.setVal(toDouble(observation.getResult()));

        LocalDateTime dateFrom = resolveDateFrom(observation);
        LocalDateTime dateTo = resolveDateTo(observation);
        measurement.setDateFrom(dateFrom);
        measurement.setDateTo(dateTo);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(MeasurementAttributeEnum.MEASURE_TYPE.name(), defaultIfBlank(datastreamConfig.getMeasureType(), Constants.MEASUREMENT_TYPE_ACTUAL));
        metadata.put(Constants.MEASUREMENT_SOURCE, defaultIfBlank(datastreamConfig.getSource(), "FROST"));
        metadata.put(FROST_OBSERVATION_ID, entityIdValue(observation));
        metadata.put(FROST_DATASTREAM_ID, entityIdValue(datastream));
        metadata.put(FROST_DATASTREAM_NAME, datastream != null ? datastream.getName() : null);
        metadata.put(FROST_THING_ID, entityIdValue(thing));
        metadata.put(FROST_THING_NAME, thing != null ? thing.getName() : null);
        metadata.put(FROST_SENSOR_ID, entityIdValue(sensor));
        metadata.put(FROST_SENSOR_NAME, sensor != null ? sensor.getName() : null);
        metadata.put(FROST_OBSERVED_PROPERTY_ID, entityIdValue(observedProperty));
        metadata.put(FROST_OBSERVED_PROPERTY_NAME, observedProperty != null ? observedProperty.getName() : null);
        metadata.put(FROST_RESULT_TIME, observation.getResultTime() != null ? observation.getResultTime().toString() : null);
        metadata.put(FROST_PARAMETERS, observation.getParameters());
        metadata.put(FROST_CITY, datastreamConfig.getCity());
        metadata.put(FROST_CONFIGURED_OBSERVED_PROPERTY, datastreamConfig.getObservedProperty());
        metadata.put(FROST_DATASTREAM_DESCRIPTION, datastreamConfig.getDescription());
        measurement.setMetadata(metadata);

        log.debug(
                "Mapped FROST observation id={} to measurement sensorId={}, datastreamId={}, value={}",
                entityIdValue(observation),
                datastreamConfig.getSensorId(),
                entityIdValue(datastream),
                measurement.getVal()
        );
        return measurement;
    }

    private Datastream getDatastream(Observation observation) {
        try {
            return observation.getDatastream();
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to resolve FROST datastream from observation", ex);
        }
    }

    private Thing getThing(Datastream datastream) {
        if (datastream == null) {
            return null;
        }
        try {
            return datastream.getThing();
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to resolve FROST thing from datastream", ex);
        }
    }

    private Sensor getSensor(Datastream datastream) {
        if (datastream == null) {
            return null;
        }
        try {
            return datastream.getSensor();
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to resolve FROST sensor from datastream", ex);
        }
    }

    private ObservedProperty getObservedProperty(Datastream datastream) {
        if (datastream == null) {
            return null;
        }
        try {
            return datastream.getObservedProperty();
        } catch (ServiceFailureException ex) {
            throw new RuntimeException("Unable to resolve FROST observed property from datastream", ex);
        }
    }

    private Double toDouble(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof Number number) {
            return number.doubleValue();
        }
        if (result instanceof String value && StringUtils.hasText(value)) {
            return Double.valueOf(value.trim());
        }
        throw new IllegalArgumentException("Unsupported FROST result type: " + result.getClass().getName());
    }

    private LocalDateTime resolveDateFrom(Observation observation) {
        TimeObject phenomenonTime = observation.getPhenomenonTime();
        if (phenomenonTime != null) {
            if (phenomenonTime.isInterval()) {
                Interval interval = phenomenonTime.getAsInterval();
                return toUtcLocalDateTime(interval.getStart());
            }
            return toLocalDateTime(phenomenonTime.getAsDateTime());
        }
        return toLocalDateTime(observation.getResultTime());
    }

    private LocalDateTime resolveDateTo(Observation observation) {
        TimeObject phenomenonTime = observation.getPhenomenonTime();
        if (phenomenonTime != null) {
            if (phenomenonTime.isInterval()) {
                Interval interval = phenomenonTime.getAsInterval();
                return toUtcLocalDateTime(interval.getEnd());
            }
            return toLocalDateTime(phenomenonTime.getAsDateTime());
        }
        return toLocalDateTime(observation.getResultTime());
    }

    private LocalDateTime toLocalDateTime(ZonedDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private LocalDateTime toUtcLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Long entityIdValue(Entity entity) {
        return entity == null ? null : idValue(entity.getId());
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

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}

