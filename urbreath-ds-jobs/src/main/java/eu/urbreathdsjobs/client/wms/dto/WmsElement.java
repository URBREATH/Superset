package eu.urbreathdsjobs.client.wms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WmsElement {
    private Long id;
    private String name;
    private String locator;

    @JsonProperty("element_type")
    private WmsElementType elementType;

    private WmsEntityType type;
    private String comments;
    private String updated;

    @JsonProperty("update_user")
    private String updateUser;

    @JsonProperty("validation_user")
    private String validationUser;

    private List<Double> centroid;
    private String geometry;
    private String model;
    private Long pk;

    @JsonProperty("pending_changes")
    private String pendingChanges;

    private List<Object> data;
    private List<Object> related;
}

