package eu.urbreathdsjobs.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.urbreathdsjobs.model.Measurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeasurementDao {

    private static final String INSERT_MEASUREMENT = """
            INSERT INTO public.measurement
            (id_param, id_sensor, period, date_from, date_to, min, q02, q24, median, q75, q98, max, avg, sd, val, metadata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String DELETE_BY_ID_PARAM = """
            DELETE FROM public.measurement WHERE id_param = ?
            """;

    private static final String DELETE_BY_ID_SENSOR_IN_PREFIX = "DELETE FROM public.measurement WHERE id_sensor IN (";
    private static final String DELETE_BY_ID_SENSOR_IN_SUFFIX = ")";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public int deleteByIdParam(Long idParam) {
        int deleted = jdbcTemplate.update(DELETE_BY_ID_PARAM, idParam);
        log.info("Deleted {} measurements for id_param={}", deleted, idParam);
        return deleted;
    }

    public int deleteBySensorIds(List<Long> sensorIds) {
        if (sensorIds == null || sensorIds.isEmpty()) {
            return 0;
        }

        List<Long> distinctSensorIds = sensorIds.stream().distinct().collect(Collectors.toList());
        String placeholders = String.join(",", Collections.nCopies(distinctSensorIds.size(), "?"));
        String sql = DELETE_BY_ID_SENSOR_IN_PREFIX + placeholders + DELETE_BY_ID_SENSOR_IN_SUFFIX;

        int deleted = jdbcTemplate.update(sql, distinctSensorIds.toArray());
        log.info("Deleted {} measurements for id_sensor IN {}", deleted, distinctSensorIds);
        return deleted;
    }

    public void batchInsert(List<? extends Measurement> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                INSERT_MEASUREMENT,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Measurement m = items.get(i);

                        ps.setLong(1, m.getIdParam());
                        ps.setLong(2, m.getIdSensor());
                        ps.setString(3, m.getPeriod());
                        ps.setObject(4, m.getDateFrom());
                        ps.setObject(5, m.getDateTo());
                        ps.setObject(6, m.getMin());
                        ps.setObject(7, m.getQ02());
                        ps.setObject(8, m.getQ24());
                        ps.setObject(9, m.getMedian());
                        ps.setObject(10, m.getQ75());
                        ps.setObject(11, m.getQ98());
                        ps.setObject(12, m.getMax());
                        ps.setObject(13, m.getAvg());
                        ps.setObject(14, m.getSd());
                        ps.setObject(15, m.getVal());
                        ps.setObject(16, toJsonb(m.getMetadata()));
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                }
        );
    }

    private PGobject toJsonb(Map<String, Object> metadata) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");

        try {
            jsonObject.setValue(mapper.writeValueAsString(metadata == null ? Map.of() : metadata));
            return jsonObject;
        } catch (JsonProcessingException e) {
            throw new SQLException("Errore serializzazione JSON", e);
        }
    }
}

