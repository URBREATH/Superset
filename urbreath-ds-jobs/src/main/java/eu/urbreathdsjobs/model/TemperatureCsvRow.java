package eu.urbreathdsjobs.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class TemperatureCsvRow implements Serializable {

	private Integer year;
	private Integer month;
	private Integer day;
	private Double tmin;
	private Double tmax;

	public TemperatureCsvRow() {}
	
	


}
