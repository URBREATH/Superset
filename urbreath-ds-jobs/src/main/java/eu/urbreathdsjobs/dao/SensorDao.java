package eu.urbreathdsjobs.dao;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.urbreathdsjobs.model.Sensor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorDao {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    private static final String FIND_SENSORS_WITH_EXTERNAL_ID = """
            SELECT id_sensor, "name", id_param, latitude, longitude, display_name, id_location, metadata::text AS metadata
            FROM public.sensor
            WHERE metadata->>'SENSOR_ID_EXTERNAL' IS NOT NULL
            """;

    private static final String INSERT_SENSOR = """
            INSERT INTO public.sensor
            (id_sensor, "name", id_param, latitude, longitude, display_name, id_location, metadata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_SENSOR_BY_EXTERNAL_ID = """
            SELECT id_sensor, "name", id_param, latitude, longitude, display_name, id_location, metadata::text AS metadata
            FROM public.sensor
            WHERE metadata->>'SENSOR_ID_EXTERNAL' = ?
            """;

    private static final String UPDATE_LAST_OBSERVATION_DATE = """
            UPDATE public.sensor
            SET metadata = jsonb_set(
                COALESCE(metadata, '{}'::jsonb),
                '{LAST_OBSERVATION_DATE}',
                to_jsonb(?::text),
                true
            )
            WHERE id_sensor = ?
            """;

    private static final String UPDATE_SENSOR_EXTERNAL_ID = """
            UPDATE public.sensor
            SET metadata = jsonb_set(
                COALESCE(metadata, '{}'::jsonb),
                '{SENSOR_ID_EXTERNAL}',
                to_jsonb(?::text),
                true
            )
            WHERE id_sensor = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Ritorna tutti i sensori che hanno il metadata SENSOR_ID_EXTERNAL valorizzato.
     *
     * @return lista di sensori con SENSOR_ID_EXTERNAL presente nel metadata
     */
    public List<Sensor> findSensorsWithExternalId() {
        return jdbcTemplate.query(FIND_SENSORS_WITH_EXTERNAL_ID, (rs, rowNum) -> mapSensor(rs));
    }

    public Optional<Sensor> findSensorByExternalId(String externalSensorId) {
        if (externalSensorId == null || externalSensorId.trim().isEmpty()) {
            return Optional.empty();
        }

        List<Sensor> sensors = jdbcTemplate.query(
                FIND_SENSOR_BY_EXTERNAL_ID,
                (rs, rowNum) -> mapSensor(rs),
                externalSensorId.trim()
        );

        if (sensors.size() > 1) {
            log.warn("Found {} sensors for SENSOR_ID_EXTERNAL={} (expected at most one)", sensors.size(), externalSensorId);
        }

        return sensors.stream().findFirst();
    }

    private Sensor mapSensor(ResultSet rs) throws SQLException {
        Sensor sensor = new Sensor();
        sensor.setIdSensor(rs.getLong("id_sensor"));
        sensor.setName(rs.getString("name"));
        sensor.setIdParam(rs.getLong("id_param"));
        sensor.setLatitude(rs.getObject("latitude", Double.class));
        sensor.setLongitude(rs.getObject("longitude", Double.class));
        sensor.setDisplayName(rs.getString("display_name"));
        sensor.setIdLocation(rs.getLong("id_location"));

        String metadataJson = rs.getString("metadata");
        if (metadataJson != null) {
            try {
                Map<String, Object> metadata = mapper.readValue(metadataJson, new TypeReference<>() {});
                sensor.setMetadata(metadata);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse sensor metadata for id_sensor={}", sensor.getIdSensor(), e);
            }
        }
        return sensor;
    }

    /**
     * Verifica se un sensore con il dato ID esterno esiste già nel database.
     *
     * @param externalSensorId identificativo esterno del sensore da cercare nel metadata
     * @return true se il sensore NON esiste (è possibile inserirlo), false se esiste già
     */
    public boolean isSensorNotExists(String externalSensorId) {
        if (externalSensorId == null || externalSensorId.trim().isEmpty()) {
            log.warn("externalSensorId è vuoto o null, considerato come non trovato");
            return true;
        }

        String sql = "SELECT COUNT(*) FROM public.sensor WHERE metadata->>'SENSOR_ID_EXTERNAL' = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, externalSensorId);

        boolean notExists = (count == null || count == 0);

        if (notExists) {
            log.debug("Sensore con SENSOR_ID_EXTERNAL={} non esiste, è possibile inserirlo", externalSensorId);
        } else {
            log.info("Sensore con SENSOR_ID_EXTERNAL={} esiste già, skippo l'inserimento", externalSensorId);
        }

        return notExists;
    }

    /**
     * Verifica se un sensore con il dato ID esterno esiste già nel database.
     * Metodo alternativo che ritorna il risultato inverso per logica più intuitiva.
     *
     * @param externalSensorId identificativo esterno del sensore da cercare nel metadata
     * @return true se il sensore esiste già, false se non esiste
     */
    public boolean sensorExists(String externalSensorId) {
        return !isSensorNotExists(externalSensorId);
    }

    /**
     * Ritorna il numero di sensori con il dato ID esterno.
     *
     * @param externalSensorId identificativo esterno del sensore
     * @return numero di sensori trovati (dovrebbe essere 0 o 1)
     */
    public int countSensorByExternalId(String externalSensorId) {
        if (externalSensorId == null || externalSensorId.trim().isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM public.sensor WHERE metadata->>'SENSOR_ID_EXTERNAL' = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, externalSensorId);
    }

    public Sensor insertSensor(Sensor sensor) {
        Objects.requireNonNull(sensor, "sensor is required");
        Objects.requireNonNull(sensor.getIdParam(), "sensor.idParam is required");
        Objects.requireNonNull(sensor.getIdLocation(), "sensor.idLocation is required");

        if (sensor.getIdSensor() == null) {
            sensor.setIdSensor(SNOWFLAKE.nextId());
        }

        jdbcTemplate.update(INSERT_SENSOR, ps -> setSensorValues(ps, sensor));

        log.info("Inserted sensor id_sensor={} for id_param={} and id_location={}",
                sensor.getIdSensor(), sensor.getIdParam(), sensor.getIdLocation());
        return sensor;
    }

    public void updateLastObservationDate(Long sensorId, String lastObservationDate) {
        Objects.requireNonNull(sensorId, "sensorId is required");
        Objects.requireNonNull(lastObservationDate, "lastObservationDate is required");

        int updated = jdbcTemplate.update(UPDATE_LAST_OBSERVATION_DATE, lastObservationDate, sensorId);
        if (updated == 0) {
            throw new IllegalStateException("No sensor updated for id_sensor=" + sensorId);
        }
        log.info("Updated LAST_OBSERVATION_DATE for sensor id_sensor={} to {}", sensorId, lastObservationDate);
    }

    public List<Long> findSensorIdsMissingExternalByFingerprint(String name, String displayName, Double latitude, Double longitude) {
        StringBuilder sql = new StringBuilder("""
                SELECT id_sensor
                FROM public.sensor
                WHERE (
                    metadata IS NULL
                    OR metadata = '{}'::jsonb
                    OR metadata->>'SENSOR_ID_EXTERNAL' IS NULL
                )
                """);

        List<Object> args = new ArrayList<>();
        if (name != null) {
            sql.append(" AND name = ?");
            args.add(name);
        }
        if (displayName != null) {
            sql.append(" AND display_name = ?");
            args.add(displayName);
        }
        if (latitude != null) {
            sql.append(" AND latitude BETWEEN ? AND ?");
            args.add(latitude - 0.000001d);
            args.add(latitude + 0.000001d);
        }
        if (longitude != null) {
            sql.append(" AND longitude BETWEEN ? AND ?");
            args.add(longitude - 0.000001d);
            args.add(longitude + 0.000001d);
        }
        sql.append(" ORDER BY id_sensor");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> rs.getLong("id_sensor"), args.toArray());
    }

    public void updateSensorExternalId(Long sensorId, String externalSensorId) {
        Objects.requireNonNull(sensorId, "sensorId is required");
        Objects.requireNonNull(externalSensorId, "externalSensorId is required");

        int updated = jdbcTemplate.update(UPDATE_SENSOR_EXTERNAL_ID, externalSensorId, sensorId);
        if (updated == 0) {
            throw new IllegalStateException("No sensor updated for id_sensor=" + sensorId);
        }
        log.info("Updated SENSOR_ID_EXTERNAL for sensor id_sensor={} to {}", sensorId, externalSensorId);
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

}


