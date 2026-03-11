package eu.urbreathdsjobs.model;

import lombok.Data;

@Data
public class MeasurementAttribute {

    private Long idRecord;
    private Long idAttribute;
    private Long idMeasure;
    private String attrValueString;
    private Double attrValueNumber;

    public MeasurementAttribute() {}

}
