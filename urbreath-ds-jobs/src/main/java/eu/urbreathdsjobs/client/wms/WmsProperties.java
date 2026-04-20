package eu.urbreathdsjobs.client.wms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for WMS requests
 */
@Component
@ConfigurationProperties(prefix = "wms")
@Data
public class WmsProperties {
    private String baseUrl;
    private Map<String, CityConfig> cities = new HashMap<>();
    private Map<String, CallTypeConfig> callTypes = new HashMap<>();

    @Data
    public static class CityConfig {
        private String bbox;
        private String timezone;
        private Integer i;
        private Integer j;
    }

    @Data
    public static class CallTypeConfig {
        private String layer;
        private String key;
        private List<Integer> measures;
    }
}

