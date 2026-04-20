package eu.urbreathdsjobs.model;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;
@Data
public class Measurement {

    private Long idMeasure;
    private Long idParam;
    private Long idSensor;
    private String period;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private Double min;
    private Double q02;
    private Double q24;
    private Double median;
    private Double q75;
    private Double q98;
    private Double max;
    private Double avg;
    private Double sd;
    private Double val;
    Map<String, Object> metadata;

    

    public Measurement() {}


}
