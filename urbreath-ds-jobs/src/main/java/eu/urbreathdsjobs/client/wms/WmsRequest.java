package eu.urbreathdsjobs.client.wms;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a WMS URL request with all parameters
 */
@Data
@Builder
public class WmsRequest {
    private City city;
    private String callType;
    private Integer measure;
    private String url;
    private String layer;
    private String key;
    private String bbox;
    private String timezone;
    private Integer i;
    private Integer j;
}

