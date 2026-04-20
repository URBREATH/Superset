package eu.urbreathdsjobs.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsLayerCollection {
    private String type;
    private List<WmsFeature> features;

    @JsonProperty("layer_name")
    private String layerName;

    @JsonProperty("layer_title")
    private String layerTitle;

    @JsonProperty("has_time")
    private Boolean hasTime;
}

