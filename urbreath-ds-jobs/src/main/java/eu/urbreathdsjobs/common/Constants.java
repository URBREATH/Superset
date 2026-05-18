package eu.urbreathdsjobs.common;

import java.util.Set;

public class Constants {
	
	public final static String MEASUREMENT_TYPE_PROJECTION = "PROJECTION";
	public final static String MEASUREMENT_TYPE_ACTUAL = "ACTUAL";
	public final static String MEASUREMENT_COD_SCENARIO = "SCENARIO";
	public final static String MEASUREMENT_SOURCE = "SOURCE";
	
	public final static String PARAM_ID = "PARAM_ID";
	public final static String SENSOR_ID = "SENSOR_ID";
	public final static String PERIOD = "PERIOD";

	public static final Set<String> ACCEPTED_UNIT_SYMBOLS = Set.of(
		"°C",
		"%",
		"hPa",
		"µg/m³",
		"ppb",
		"dB",
		"ppm",
		//"V",
		//"N/A",
		"ppl"
		//"dBm",
		//"1",
		//"m/s"
	);

}
