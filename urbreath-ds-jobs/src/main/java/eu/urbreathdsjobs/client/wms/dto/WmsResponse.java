package eu.urbreathdsjobs.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsResponse {
    private String type;
    private List<WmsLayerCollection> features;
}

