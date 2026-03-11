package eu.urbreathdsjobs.model;

import java.util.List;

import lombok.Data;

@Data
public class TrafficHistogram {
	
	private Long idTrafficMeasure;
	private String histogramType;
	private Integer bucketIndex;
	private Double value;

}
