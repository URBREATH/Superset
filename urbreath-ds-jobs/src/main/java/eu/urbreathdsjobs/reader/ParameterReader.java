package eu.urbreathdsjobs.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParameterReader {

    private static final String FIND_ID_BY_NATURAL_KEY = """
            SELECT id_param
            FROM public."parameter"
            WHERE name IS NOT DISTINCT FROM ?
              AND units IS NOT DISTINCT FROM ?
              AND display_name IS NOT DISTINCT FROM ?
            ORDER BY id_param
            LIMIT 1
            """;

    private final JdbcTemplate jdbcTemplate;

    public boolean parameterExists(String name, String units, String displayName) {
        return findIdByNameUnitsDisplayName(name, units, displayName) != null;
    }

    public Long findIdByNameUnitsDisplayName(String name, String units, String displayName) {
        return jdbcTemplate.query(
                FIND_ID_BY_NATURAL_KEY,
                rs -> rs.next() ? rs.getLong("id_param") : null,
                name,
                units,
                displayName);
    }
}

