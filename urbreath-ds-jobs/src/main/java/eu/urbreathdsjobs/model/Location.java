package eu.urbreathdsjobs.model;

import lombok.Data;

@Data
public class Location {

	private Long idLocation;
	private String name;
	private String locality;
	private String timezone;
	private Double latitude;
	private Double longitude;
	private Long idCountry;
	private Long idZone;
	private Long idCity;

}

