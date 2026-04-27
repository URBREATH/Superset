package eu.urbreathdsjobs.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.postgresql.util.PGobject;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.urbreathdsjobs.model.Measurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public void deleteByIdParam(Long idParam) {
        int deleted = jdbcTemplate.update(DELETE_BY_ID_PARAM, idParam);
        log.info("Deleted {} measurements for id_param={}", deleted, idParam);
    }

    private static final int CHUNK_SIZE = 500;

    @Transactional
    public void deleteAndWrite(Long idParam, List<Measurement> measurements) throws Exception {
        deleteByIdParam(idParam);
        int total = measurements.size();
        for (int i = 0; i < total; i += CHUNK_SIZE) {
            List<Measurement> chunk = measurements.subList(i, Math.min(i + CHUNK_SIZE, total));
            write(new Chunk<>(chunk));
            log.debug("Written chunk [{}-{}] of {}", i, i + chunk.size(), total);
        }
        log.info("deleteAndWrite completed for id_param={}, inserted={}", idParam, total);
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
