package eu.urbreathdsjobs.model;

import lombok.Data;

@Data
public class TrafficHistogram {
	
	private Long idTrafficMeasure;
	private String histogramType;
	private Integer bucketIndex;
	private Double value;

}
