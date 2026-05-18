package eu.urbreathdsjobs.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Objects;

import eu.urbreathdsjobs.model.Location;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import eu.urbreathdsjobs.model.City;
import eu.urbreathdsjobs.model.Parameter;
import eu.urbreathdsjobs.model.Sensor;
import eu.urbreathdsjobs.reader.CityReader;
import eu.urbreathdsjobs.reader.LocationReader;
import eu.urbreathdsjobs.reader.ParameterReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SensorWriter {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    private static final String INSERT_PARAMETER = """
            INSERT INTO public."parameter"
            (id_param, "name", units, display_name, description)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String INSERT_LOCATION = """
            INSERT INTO public."location"
            (id_location, "name", locality, timezone, latitude, longitude, id_country, id_zone, id_city)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_SENSOR = """
            INSERT INTO public.sensor
            (id_sensor, "name", id_param, latitude, longitude, display_name, id_location, metadata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final CityReader cityReader;
    private final ParameterReader parameterReader;
    private final LocationReader locationReader;
    private final ObjectMapper mapper = new ObjectMapper();

    public void insertParameter(Parameter parameter) {
        validateParameter(parameter);

        if (parameter.getIdParam() == null) {
            parameter.setIdParam(nextId());
        }

        jdbcTemplate.update(INSERT_PARAMETER,
                parameter.getIdParam(),
                parameter.getName(),
                parameter.getUnits(),
                parameter.getDisplayName(),
                parameter.getDescription());

        log.info("Inserted parameter id_param={}", parameter.getIdParam());
    }

    public void insertLocation(Location location) {
        validateLocation(location);

        if (location.getIdLocation() == null) {
            location.setIdLocation(nextId());
        }

        jdbcTemplate.update(INSERT_LOCATION, ps -> setLocationValues(ps, location));

        log.info("Inserted location id_location={}", location.getIdLocation());
    }

    public Sensor insertSensor(Sensor sensor) {
        validateSensor(sensor);

        if (sensor.getIdSensor() == null) {
            sensor.setIdSensor(nextId());
        }

        jdbcTemplate.update(INSERT_SENSOR, ps -> setSensorValues(ps, sensor));

        log.info("Inserted sensor id_sensor={} for id_param={} and id_location={}",
                sensor.getIdSensor(), sensor.getIdParam(), sensor.getIdLocation());
        return sensor;
    }

    public Sensor insertSensor(Parameter parameter, Location location, Sensor sensor, City city) {
        Objects.requireNonNull(parameter, "parameter is required");
        Objects.requireNonNull(location, "location is required");
        Objects.requireNonNull(sensor, "sensor is required");

        String cityName = city != null ? city.getName() : null;
        City resolvedCity = cityReader.findByNameOrFallback(cityName);
        if (resolvedCity == null) {
            throw new IllegalStateException("Unable to resolve city from DB and fallback 'n/a'");
        }

        location.setIdCity(resolvedCity.getIdCity());
        location.setIdCountry(resolvedCity.getIdCountry());
        if (city != null) {
            city.setIdCity(resolvedCity.getIdCity());
            city.setIdCountry(resolvedCity.getIdCountry());
            city.setName(resolvedCity.getName());
        }

        Long existingParameterId = parameterReader.findIdByNameUnitsDisplayName(
                parameter.getName(),
                parameter.getUnits(),
                parameter.getDisplayName());

        if (existingParameterId != null) {
            parameter.setIdParam(existingParameterId);
            log.info("Reusing existing parameter id_param={} for name={}, units={}, displayName={}",
                    existingParameterId,
                    parameter.getName(),
                    parameter.getUnits(),
                    parameter.getDisplayName());
        } else {
            insertParameter(parameter);
        }

        Long existingLocationId = locationReader.findIdByCoordinates(location.getLatitude(), location.getLongitude());
        if (existingLocationId != null) {
            location.setIdLocation(existingLocationId);
            log.info("Reusing existing location id_location={} for latitude={}, longitude={}",
                    existingLocationId,
                    location.getLatitude(),
                    location.getLongitude());
        } else {
            insertLocation(location);
        }

        alignSensorReferences(sensor, parameter, location);
        return insertSensor(sensor);
    }

    private void alignSensorReferences(Sensor sensor, Parameter parameter, Location location) {
        if (sensor.getIdParam() == null) {
            sensor.setIdParam(parameter.getIdParam());
        } else if (!sensor.getIdParam().equals(parameter.getIdParam())) {
            throw new IllegalArgumentException("sensor.idParam does not match parameter.idParam");
        }

        if (sensor.getIdLocation() == null) {
            sensor.setIdLocation(location.getIdLocation());
        } else if (!sensor.getIdLocation().equals(location.getIdLocation())) {
            throw new IllegalArgumentException("sensor.idLocation does not match location.idLocation");
        }
    }

    private void validateParameter(Parameter parameter) {
        Objects.requireNonNull(parameter, "parameter is required");
    }

    private void validateLocation(Location location) {
        Objects.requireNonNull(location, "location is required");
        Objects.requireNonNull(location.getIdCountry(), "location.idCountry is required");
    }

    private void validateSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "sensor is required");
        Objects.requireNonNull(sensor.getIdParam(), "sensor.idParam is required");
        Objects.requireNonNull(sensor.getIdLocation(), "sensor.idLocation is required");
    }

    private void setLocationValues(PreparedStatement ps, Location location) throws SQLException {
        ps.setLong(1, location.getIdLocation());
        ps.setString(2, location.getName());
        ps.setString(3, location.getLocality());
        ps.setString(4, location.getTimezone());
        ps.setObject(5, location.getLatitude());
        ps.setObject(6, location.getLongitude());
        ps.setLong(7, location.getIdCountry());
        ps.setObject(8, location.getIdZone(), Types.BIGINT);
        ps.setObject(9, location.getIdCity(), Types.BIGINT);
    }

    private void setSensorValues(PreparedStatement ps, Sensor sensor) throws SQLException {
        ps.setLong(1, sensor.getIdSensor());
        ps.setString(2, sensor.getName());
        ps.setLong(3, sensor.getIdParam());
        ps.setObject(4, sensor.getLatitude());
        ps.setObject(5, sensor.getLongitude());
        ps.setString(6, sensor.getDisplayName());
        ps.setLong(7, sensor.getIdLocation());
        ps.setObject(8, toJsonb(sensor.getMetadata()));
    }

    private PGobject toJsonb(Map<String, Object> metadata) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");

        try {
            jsonObject.setValue(mapper.writeValueAsString(metadata == null ? Map.of() : metadata));
            return jsonObject;
        } catch (JsonProcessingException e) {
            throw new SQLException("Errore serializzazione JSON metadata sensor", e);
        }
    }

    private long nextId() {
        return SNOWFLAKE.nextId();
    }

}
