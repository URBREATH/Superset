package eu.urbreathdsjobs.model;

import lombok.Data;

@Data
public class PrecipitationCsvRow {
	private Integer year;
	private Integer month;
	private Integer day;
	private Double mmRain;

	public PrecipitationCsvRow() {}
}
