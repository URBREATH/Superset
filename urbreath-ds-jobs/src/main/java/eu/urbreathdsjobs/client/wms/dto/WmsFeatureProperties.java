package eu.urbreathdsjobs.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsFeatureProperties {
    private WmsElement element;
    private Long id;

    @JsonProperty("base_time")
    private String baseTime;

    private Double value;

    @JsonProperty("dimension_value")
    private Map<String, JsonNode> dimensionValue;

    @JsonProperty("system_date")
    private String systemDate;

    private Double minutes;
    private String time;
}

