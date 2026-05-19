package eu.urbreathdsjobs.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TrafficMeasurement {

	private Long idTrafficMeasure;
    private String segmentId;
    private LocalDateTime date;
    private String interval;
    private Double uptime;
    private Double direction;
    private String timezone;

    private Double heavy;
    private Double car;
    private Double bike;
    private Double pedestrian;
    private Double night;

    private Double heavyLft;
    private Double heavyRgt;
    private Double carLft;
    private Double carRgt;
    private Double bikeLft;
    private Double bikeRgt;
    private Double pedestrianLft;
    private Double pedestrianRgt;
    private Double nightLft;
    private Double nightRgt;

    private Double v85;

    private List<TrafficHistogram> carSpeedHist0to70plus;
    private List<TrafficHistogram> carSpeedHist0to120plus;
    
}
