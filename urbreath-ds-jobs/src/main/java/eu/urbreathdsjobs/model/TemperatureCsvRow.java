package eu.urbreathdsjobs.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class TemperatureCsvRow implements Serializable {

	private Integer year;
	private Integer month;
	private Integer day;
	private Double tmin;
	private Double tmax;

	public TemperatureCsvRow() {}
	
	


}
