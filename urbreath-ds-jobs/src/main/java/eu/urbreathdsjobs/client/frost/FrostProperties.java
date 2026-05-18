package eu.urbreathdsjobs.client.frost;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "frost")
@Data
public class FrostProperties {

    // TUTTE le proprietà vengono lette dal file .yaml
    // in mancanza di tali proprietà questi sono i valori di DEAFULT
    private boolean enabled;
    private String baseUrl;
    private Long idParam;
    private Integer pageSize = 500;
    private boolean followPaginationLinks = true;
    private String orderBy = "phenomenonTime desc";
    private String expand = "Datastream($expand=Thing,Sensor,ObservedProperty)";
    private String filter = "result ne null";
    private List<DatastreamConfig> datastreams = new ArrayList<>();

    public List<DatastreamConfig> getEnabledDatastreams() {
        return datastreams.stream()
                .filter(DatastreamConfig::isEnabled)
                .peek(config -> {
                    if (!StringUtils.hasText(config.getFilter()) && StringUtils.hasText(filter)) {
                        config.setFilter(filter);
                    }
                })
                .toList();
    }

    @Data
    public static class DatastreamConfig {
        private boolean enabled = true;
        private Long datastreamId;
        private Long sensorId;
        private String city;
        private String description;
        private String observedProperty;
        private String measureType = "ACTUAL";
        private String source = "FROST";
        private String filter;
    }
}

