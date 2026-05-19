package eu.urbreathdsjobs.client.wms;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for WMS client beans
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class WmsConfiguration {

    private static final Duration WMS_CONNECT_TIMEOUT = Duration.ofSeconds(20);

    private final WmsUrlService wmsUrlService;

    /**
     * Creates a bean containing all WMS URLs for Madrid
     */
    @Bean(name = "madridWmsUrls")
    public List<WmsRequest> madridWmsUrls() {
        log.info("Creating Madrid WMS URLs bean");
        return wmsUrlService.generateUrlsForCity(City.MADRID);
    }

    /**
     * Creates a bean containing all WMS URLs for Leuven
     */
    @Bean(name = "leuvenWmsUrls")
    public List<WmsRequest> leuvenWmsUrls() {
        log.info("Creating Leuven WMS URLs bean");
        return wmsUrlService.generateUrlsForCity(City.LEUVEN);
    }

    /**
     * Creates a bean containing all WMS URLs for Cluj
     */
    @Bean(name = "clujWmsUrls")
    public List<WmsRequest> clujWmsUrls() {
        log.info("Creating Cluj WMS URLs bean");
        return wmsUrlService.generateUrlsForCity(City.CLUJ);
    }

    /**
     * Creates a bean containing all WMS URLs for Tallin
     */
    @Bean(name = "tallinWmsUrls")
    public List<WmsRequest> tallinWmsUrls() {
        log.info("Creating Tallin WMS URLs bean");
        return wmsUrlService.generateUrlsForCity(City.TALLIN);
    }

    /**
     * Creates a bean containing all WMS URLs for all cities
     */
    @Bean(name = "allWmsUrls")
    public List<WmsRequest> allWmsUrls() {
        log.info("Creating all WMS URLs bean");
        return wmsUrlService.generateUrlsForAllCities();
    }

    /**
     * Creates a map of city to WMS URLs for easy access
     */
    @Bean(name = "cityWmsUrlsMap")
    public Map<City, List<WmsRequest>> cityWmsUrlsMap() {
        Map<City, List<WmsRequest>> map = new HashMap<>();
        for (City city : City.values()) {
            map.put(city, wmsUrlService.generateUrlsForCity(city));
        }
        log.info("Created WMS URLs map with {} cities", map.size());
        return map;
    }

    /**
     * Provides a Jackson mapper for WMS JSON parsing in non-web Spring Boot setups.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(WMS_CONNECT_TIMEOUT)
                .build();
    }
}

