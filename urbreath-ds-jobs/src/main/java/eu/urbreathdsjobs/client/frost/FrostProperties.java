package eu.urbreathdsjobs.client.frost;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "frost")
@Data
public class FrostProperties {

    private boolean enabled;
    private String baseUrl;
    private Long idParam;
    private Integer pageSize = 500;
    private String orderBy = "phenomenonTime desc";
    private String expand = "Datastream($expand=Thing,Sensor,ObservedProperty)";
    private List<DatastreamConfig> datastreams = new ArrayList<>();

    public List<DatastreamConfig> getEnabledDatastreams() {
        return datastreams.stream()
                .filter(DatastreamConfig::isEnabled)
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

