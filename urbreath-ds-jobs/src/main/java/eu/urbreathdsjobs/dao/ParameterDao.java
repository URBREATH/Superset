package eu.urbreathdsjobs.dao;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import eu.urbreathdsjobs.model.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParameterDao {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    private static final String FIND_ID_BY_NATURAL_KEY = """
            SELECT id_param
            FROM public."parameter"
            WHERE name IS NOT DISTINCT FROM ?
              AND units IS NOT DISTINCT FROM ?
              AND display_name IS NOT DISTINCT FROM ?
            ORDER BY id_param
            LIMIT 1
            """;

    private static final String INSERT_PARAMETER = """
            INSERT INTO public."parameter"
            (id_param, "name", units, display_name, description)
            VALUES (?, ?, ?, ?, ?)
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

    public Parameter insertParameter(Parameter parameter) {
        Objects.requireNonNull(parameter, "parameter is required");

        if (parameter.getIdParam() == null) {
            parameter.setIdParam(SNOWFLAKE.nextId());
        }

        jdbcTemplate.update(INSERT_PARAMETER,
                parameter.getIdParam(),
                parameter.getName(),
                parameter.getUnits(),
                parameter.getDisplayName(),
                parameter.getDescription());

        log.info("Inserted parameter id_param={}", parameter.getIdParam());
        return parameter;
    }
}


