package eu.urbreathdsjobs.dao;

import eu.urbreathdsjobs.model.TrafficMeasurement;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TrafficMeasurementDao {

    private static final String INSERT_TRAFFIC_MEASUREMENT = """
            INSERT INTO traffic_measurement(id_traffic_measure,
              segment_id,date,interval,uptime,direction,timezone,
              heavy,car,bike,pedestrian,night,
              heavy_lft,heavy_rgt,car_lft,car_rgt,
              bike_lft,bike_rgt,pedestrian_lft,pedestrian_rgt,
              night_lft,night_rgt,v85
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<? extends TrafficMeasurement> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                INSERT_TRAFFIC_MEASUREMENT,
                items,
                items.size(),
                (ps, m) -> {
                    ps.setLong(1, m.getIdTrafficMeasure());
                    ps.setString(2, m.getSegmentId());
                    ps.setObject(3, m.getDate());
                    ps.setString(4, m.getInterval());
                    ps.setObject(5, m.getUptime());
                    ps.setObject(6, m.getDirection());
                    ps.setString(7, m.getTimezone());
                    ps.setObject(8, m.getHeavy());
                    ps.setObject(9, m.getCar());
                    ps.setObject(10, m.getBike());
                    ps.setObject(11, m.getPedestrian());
                    ps.setObject(12, m.getNight());
                    ps.setObject(13, m.getHeavyLft());
                    ps.setObject(14, m.getHeavyRgt());
                    ps.setObject(15, m.getCarLft());
                    ps.setObject(16, m.getCarRgt());
                    ps.setObject(17, m.getBikeLft());
                    ps.setObject(18, m.getBikeRgt());
                    ps.setObject(19, m.getPedestrianLft());
                    ps.setObject(20, m.getPedestrianRgt());
                    ps.setObject(21, m.getNightLft());
                    ps.setObject(22, m.getNightRgt());
                    ps.setObject(23, m.getV85());
                }
        );
    }
}

