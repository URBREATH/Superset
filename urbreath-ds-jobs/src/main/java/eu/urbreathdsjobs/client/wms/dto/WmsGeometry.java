package eu.urbreathdsjobs.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsGeometry {
    private String type;
    private List<Double> coordinates;
}

