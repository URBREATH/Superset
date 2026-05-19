package eu.urbreathdsjobs.dao;

import eu.urbreathdsjobs.model.City;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CityDao {

    private static final String FALLBACK_CITY_NAME = "n/a";

    private static final String FIND_BY_NAME_IGNORE_CASE = """
            SELECT id_city, name, id_country
            FROM public.city
            WHERE LOWER(name) = LOWER(?)
            ORDER BY id_city
            LIMIT 1
            """;

    private static final RowMapper<City> CITY_ROW_MAPPER = (rs, rowNum) -> {
        City city = new City();
        city.setIdCity(rs.getLong("id_city"));
        city.setName(rs.getString("name"));
        city.setIdCountry(rs.getLong("id_country"));
        return city;
    };

    private final JdbcTemplate jdbcTemplate;

    public City findByNameOrFallback(String cityName) {
        City city = findByNameIgnoreCase(cityName);
        if (city != null) {
            return city;
        }

        City fallbackCity = findByNameIgnoreCase(FALLBACK_CITY_NAME);
        if (fallbackCity != null) {
            log.debug("City '{}' non trovata, uso fallback '{}' (id_city={})",
                    cityName,
                    fallbackCity.getName(),
                    fallbackCity.getIdCity());
            return fallbackCity;
        }

        log.warn("City '{}' non trovata e fallback '{}' assente in public.city",
                cityName,
                FALLBACK_CITY_NAME);
        return null;
    }

    public City findByNameIgnoreCase(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return null;
        }

        List<City> rows = jdbcTemplate.query(
                FIND_BY_NAME_IGNORE_CASE,
                CITY_ROW_MAPPER,
                cityName.trim());

        return rows.isEmpty() ? null : rows.get(0);
    }
}


