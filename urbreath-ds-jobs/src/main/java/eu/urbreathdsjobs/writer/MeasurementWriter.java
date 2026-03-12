package eu.urbreathdsjobs.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.model.MeasurementAttribute;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class MeasurementWriter implements ItemWriter<Measurement> {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_MEASUREMENT = """
            INSERT INTO public.measurement
            (id_param, id_sensor, period, date_from, date_to, min, q02, q24, median, q75, q98, max, avg, sd, val)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_ATTRIBUTE = """
            INSERT INTO measurement_attribute (id_attribute, id_measure, attr_value_string, attr_value_number)
            VALUES (?, ?, ?, ?)
            """;

    @Override
    public void write(Chunk<? extends Measurement> chunk) throws Exception {
        List<? extends Measurement> items = chunk.getItems();

        // 1. Insert tutti i Measurement in un unico batch, recuperando gli id generati
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.batchUpdate(
            con -> con.prepareStatement(INSERT_MEASUREMENT, new String[]{"id_measure"}),
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
                }

                @Override
                public int getBatchSize() {
                    return items.size();
                }
            },
            keyHolder
        );

        // 2. Associa gli id generati agli item (ordine posizionale garantito)
        List<Long> ids = keyHolder.getKeyList().stream()
            .map(k -> ((Number) k.get("id_measure")).longValue())
            .toList();

        if (ids.size() != items.size()) {
            throw new IllegalStateException(
                "Mismatch tra id generati (%d) e item (%d)".formatted(ids.size(), items.size())
            );
        }

        // 3. Imposta l'id sull'oggetto e costruisce i parametri per gli attributi
        List<Object[]> attrParams = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Measurement m = items.get(i);
            Long idMeasure = ids.get(i);
            m.setIdMeasure(idMeasure);

            for (MeasurementAttribute attr : m.getAttributes()) {
                attrParams.add(new Object[]{
                    attr.getIdAttribute(),
                    idMeasure,
                    attr.getAttrValueString(),
                    attr.getAttrValueNumber()
                });
            }
        }

        // 4. Insert tutti gli attributi in un unico batch
        if (!attrParams.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_ATTRIBUTE, attrParams);
        }
    }
}
