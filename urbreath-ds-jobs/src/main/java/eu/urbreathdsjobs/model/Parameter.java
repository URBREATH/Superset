package eu.urbreathdsjobs.model;

import lombok.Data;

@Data
public class Parameter {

    private Long idParam;
    private String name;
    private String units;
    private String displayName;
    private String description;

}

