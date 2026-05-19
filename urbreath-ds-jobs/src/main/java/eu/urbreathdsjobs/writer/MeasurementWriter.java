package eu.urbreathdsjobs.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.urbreathdsjobs.model.Measurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MeasurementWriter implements ItemWriter<Measurement> {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper mapper = new ObjectMapper();


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

    public void deleteByIdParam(Long idParam) {
        int deleted = jdbcTemplate.update(DELETE_BY_ID_PARAM, idParam);
        log.info("Deleted {} measurements for id_param={}", deleted, idParam);
    }

    public void deleteBySensorIds(List<Long> sensorIds) {
        if (sensorIds == null || sensorIds.isEmpty()) {
            log.warn("No sensor ids provided for deletion. Skipping delete by id_sensor.");
            return;
        }

        List<Long> distinctSensorIds = sensorIds.stream().distinct().collect(Collectors.toList());
        String placeholders = String.join(",", Collections.nCopies(distinctSensorIds.size(), "?"));
        String sql = DELETE_BY_ID_SENSOR_IN_PREFIX + placeholders + DELETE_BY_ID_SENSOR_IN_SUFFIX;
        int deleted = jdbcTemplate.update(sql, distinctSensorIds.toArray());
        log.info("Deleted {} measurements for id_sensor IN {}", deleted, distinctSensorIds);
    }

    private static final int CHUNK_SIZE = 500;


    @Transactional
    public void deleteAndWriteBySensorIds(List<Long> sensorIds, List<Measurement> measurements) throws Exception {
        deleteBySensorIds(sensorIds);
        int total = measurements.size();
        for (int i = 0; i < total; i += CHUNK_SIZE) {
            List<Measurement> chunk = measurements.subList(i, Math.min(i + CHUNK_SIZE, total));
            write(new Chunk<>(chunk));
            log.debug("Written chunk [{}-{}] of {}", i, i + chunk.size(), total);
        }
        log.info("deleteAndWriteBySensorIds completed for sensorsCount={}, inserted={}",
                sensorIds != null ? sensorIds.size() : 0,
                total);
    }

    @Override
    public void write(Chunk<? extends Measurement> chunk) throws Exception {
        List<? extends Measurement> items = chunk.getItems();

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

                    try {
  						String json = mapper.writeValueAsString(m.getMetadata());
  						PGobject jsonObject = new PGobject();
  						jsonObject.setType("jsonb");
  						jsonObject.setValue(json);
  						
  						ps.setObject(16, jsonObject);
  					} catch (JsonProcessingException e) {
  						  throw new SQLException("Errore serializzazione JSON", e);
  					}
                }

                @Override
                public int getBatchSize() {
                    return items.size();
                }
            }
        );
    }
    
}
