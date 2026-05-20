package eu.urbreathdsjobs.common;

public enum SensorAttributeEnum {

	LAST_OBSERVATION_DATE("Ultima data delle osservazioni inserite a sistema", "DATE"),
	SENSOR_ID_EXTERNAL("Identificativo esterno del sensore da sorgenti dati esterne", "NUMBER"),
	SENSOR_TYPE("Tipologia di sensore (es. anemometro, termometro)", "STRING"),
	MANUFACTURER("Produttore del sensore", "STRING"),
	MODEL("Modello del sensore", "STRING"),
	INSTALLATION_DATE("Data di installazione del sensore", "STRING"),
	MEASUREMENT_UNIT("Unità di misura del parametro rilevato", "STRING"),
	ALTITUDE("Altitudine della location in cui è posizionato il sensore", "NUMBER"),
	SENSOR_STATUS("Stato operativo del sensore (attivo, inattivo, in manutenzione)", "STRING"),
	CALIBRATION_DATE("Data dell'ultima calibrazione", "STRING"),
	ACCURACY("Accuratezza della misurazione", "NUMBER"),
	RESPONSE_TIME("Tempo di risposta del sensore", "NUMBER"),
	MAINTENANCE_NOTES("Note su manutenzione e interventi", "STRING"),
	FIRMWARE_VERSION("Versione del firmware", "STRING"),
	BATTERY_LEVEL("Livello di batteria (se applicabile)", "NUMBER"),
	SIGNAL_STRENGTH("Potenza del segnale di trasmissione", "NUMBER"),
	LAST_SYNC("Data dell'ultimo sincronismo", "STRING");

	private final String description;
	private final String valueType;

	SensorAttributeEnum(String description, String valueType) {
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
