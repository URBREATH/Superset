package eu.urbreathdsjobs.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsEntityType {
    private String klass;
    private String name;
}

