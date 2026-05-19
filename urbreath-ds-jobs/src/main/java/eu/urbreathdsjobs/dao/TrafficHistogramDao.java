package eu.urbreathdsjobs.dao;

import eu.urbreathdsjobs.model.TrafficHistogram;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TrafficHistogramDao {

    private static final String INSERT_TRAFFIC_HISTOGRAM = """
            INSERT INTO traffic_speed_histogram(
              id_traffic_measure,histogram_type,bucket_index,value
            )
            VALUES (?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<? extends TrafficHistogram> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                INSERT_TRAFFIC_HISTOGRAM,
                items,
                items.size(),
                (ps, h) -> {
                    ps.setLong(1, h.getIdTrafficMeasure());
                    ps.setString(2, h.getHistogramType());
                    ps.setObject(3, h.getBucketIndex());
                    ps.setObject(4, h.getValue());
                }
        );
    }
}

