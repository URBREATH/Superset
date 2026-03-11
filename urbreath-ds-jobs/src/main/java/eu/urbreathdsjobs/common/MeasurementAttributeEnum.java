package eu.urbreathdsjobs.common;

public enum MeasurementAttributeEnum {

	MEASURE_TYPE(4, "Tipologia di misurazione (actual o projection)", "STRING"),
	MEASURE_SIMULATION_SOURCE(5, "Codice scenario di simulazione", "STRING"),	
	MEASURE_SIMULATION_COD_SCENARIO(6, "Codice ente che ha prodotto la simulazione", "STRING"),
	TERMIC_AVG(7, "Media termica con modello sinusoidale", "NUMBER"),	
	MEASURE_HORIZON(10, "", "NUMBER"),
	MEASURE_THRESHOLD(11, "", "NUMBER"),
	FILE_PATH(12, "Path del file da cui è stata estratta la misura", "STRING"),
	ID_BATCH(13, "Identificativo dell'istanza batch che ha eseguito il caricamento", "NUMBER");

	private final long idAttribute;
	private final String description;
	private final String valueType;
	
	private MeasurementAttributeEnum(long idAttribute, String description, String valueType) {
		this.idAttribute = idAttribute;
		this.description = description;
		this.valueType = valueType;
	}
	
		
	public long getIdAttribute() {
		return idAttribute;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getValueType() {
		return valueType;
	}
		
}
