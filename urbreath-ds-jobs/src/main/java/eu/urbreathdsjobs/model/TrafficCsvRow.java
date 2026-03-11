package eu.urbreathdsjobs.model;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TrafficCsvRow {

    private String instance_id;
    private String segment_id;
    private LocalDateTime date;
    private String interval;
    private Double uptime;

    private Double heavy;
    private Double car;
    private Double bike;
    private Double pedestrian;
    private Double night;

    private Double heavy_lft;
    private Double heavy_rgt;
    private Double car_lft;
    private Double car_rgt;
    private Double bike_lft;
    private Double bike_rgt;
    private Double pedestrian_lft;
    private Double pedestrian_rgt;
    private Double night_lft;
    private Double night_rgt;

    private Double direction;

    private String car_speed_hist_0to70plus;
    private String car_speed_hist_0to120plus;

    private String timezone;
    private Double v85;
}
