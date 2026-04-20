package eu.urbreathdsjobs.common;

public enum MeasurementAttributeEnum {

	MEASURE_TYPE( "Tipologia di misurazione (actual o projection)", "STRING"),
	MEASURE_SIMULATION_SOURCE("Codice scenario di simulazione", "STRING"),	
	MEASURE_SIMULATION_COD_SCENARIO("Codice ente che ha prodotto la simulazione", "STRING"),
	TERMIC_AVG("Media termica con modello sinusoidale", "NUMBER"),	
	MEASURE_HORIZON( "", "NUMBER"),
	MEASURE_THRESHOLD("", "NUMBER"),
	FILE_PATH("Path del file da cui è stata estratta la misura", "STRING"),
	ID_BATCH("Identificativo dell'istanza batch che ha eseguito il caricamento", "NUMBER");


	private final String description;
	private final String valueType;
	
	private MeasurementAttributeEnum( String description, String valueType) {
		this.description = description;
		this.valueType = valueType;
	}
	
		
	
	public String getDescription() {
		return description;
	}
	
	public String getValueType() {
		return valueType;
	}
		
}
