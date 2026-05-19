package eu.urbreathdsjobs.dao;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import eu.urbreathdsjobs.model.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationDao {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    private static final String FIND_ID_BY_COORDINATES = """
            SELECT id_location
            FROM public."location"
            WHERE latitude = ?
              AND longitude = ?
            ORDER BY id_location
            LIMIT 1
            """;

    private static final String INSERT_LOCATION = """
            INSERT INTO public."location"
            (id_location, "name", locality, timezone, latitude, longitude, id_country, id_zone, id_city)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public Long findIdByCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        return jdbcTemplate.query(
                FIND_ID_BY_COORDINATES,
                rs -> rs.next() ? rs.getLong("id_location") : null,
                latitude,
                longitude);
    }

    public boolean locationExists(Double latitude, Double longitude) {
        return findIdByCoordinates(latitude, longitude) != null;
    }

    public Location insertLocation(Location location) {
        Objects.requireNonNull(location, "location is required");
        Objects.requireNonNull(location.getIdCountry(), "location.idCountry is required");

        if (location.getIdLocation() == null) {
            location.setIdLocation(SNOWFLAKE.nextId());
        }

        jdbcTemplate.update(INSERT_LOCATION, ps -> setLocationValues(ps, location));
        log.info("Inserted location id_location={}", location.getIdLocation());
        return location;
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
}


