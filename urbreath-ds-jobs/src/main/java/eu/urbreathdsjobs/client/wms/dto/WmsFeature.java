package eu.urbreathdsjobs.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsFeature {
    private String type;
    private String name;
    private WmsGeometry geometry;
    private WmsFeatureProperties properties;
}

