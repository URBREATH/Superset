package eu.urbreathdsjobs.reader;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LocationReader {

    private static final String FIND_ID_BY_COORDINATES = """
            SELECT id_location
            FROM public."location"
            WHERE latitude = ?
              AND longitude = ?
            ORDER BY id_location
            LIMIT 1
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
}

