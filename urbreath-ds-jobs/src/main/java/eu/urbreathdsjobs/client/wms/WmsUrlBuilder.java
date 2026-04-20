package eu.urbreathdsjobs.client.wms;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating WMS request URLs
 */
@Data
@Builder
public class WmsUrlBuilder {
    private static final String REQUEST_PARAM = "REQUEST=GetFeatureInfo";
    private static final String SERVICE_PARAM = "SERVICE=WMS";
    private static final String VERSION_PARAM = "VERSION=1.3.0";
    private static final String FORMAT_PARAM = "FORMAT=image%2Fpng";
    private static final String STYLES_PARAM = "STYLES=";
    private static final String TRANSPARENT_PARAM = "TRANSPARENT=TRUE";
    private static final String HORIZON_PARAM = "HORIZON=__all__";
    private static final String INFO_FORMAT_PARAM = "INFO_FORMAT=application%2Fjson";
    private static final String I_PARAM = "I=";
    private static final String J_PARAM = "J=";
    private static final String WIDTH_PARAM = "WIDTH=512";
    private static final String HEIGHT_PARAM = "HEIGHT=512";
    private static final String CRS_PARAM = "CRS=EPSG%3A3857";

    private String baseUrl;
    private City city;
    private String callType;
    private String layer;
    private String key;
    private String bbox;
    private String timezone;
    private Integer measure;
    private Integer i;
    private Integer j;

    /**
     * Builds a complete WMS URL with all parameters
     *
     * @return the formatted WMS URL
     */
    public String build() {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?").append(REQUEST_PARAM);
        url.append("&").append(SERVICE_PARAM);
        url.append("&").append(VERSION_PARAM);
        url.append("&").append(FORMAT_PARAM);
        url.append("&").append(STYLES_PARAM);
        url.append("&").append(TRANSPARENT_PARAM);
        url.append("&QUERY_LAYERS=").append(layer);
        url.append("&LAYERS=").append(layer);
        url.append("&").append(HORIZON_PARAM);
        url.append("&").append(key).append("=").append(measure);
        url.append("&__tz=").append(timezone);
        url.append("&").append(INFO_FORMAT_PARAM);
        url.append("&").append(I_PARAM).append(i);
        url.append("&").append(J_PARAM).append(j);
        url.append("&").append(WIDTH_PARAM);
        url.append("&").append(HEIGHT_PARAM);
        url.append("&").append(CRS_PARAM);
        url.append("&BBOX=").append(bbox);

        return url.toString();
    }
}

