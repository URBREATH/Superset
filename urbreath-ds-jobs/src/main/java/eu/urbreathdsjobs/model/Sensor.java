package eu.urbreathdsjobs.model;

import eu.urbreathdsjobs.common.SensorAttributeEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Sensor {

    private Long idSensor;
    private String name;
    private Long idParam;
    private Double latitude;
    private Double longitude;
    private String displayName;
    private Long idLocation;
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Imposta un attributo nel metadata utilizzando l'enum SensorAttributeEnum.
     * Esempio: sensor.setMetadataAttribute(SensorAttributeEnum.SENSOR_TYPE, "anemometer");
     *
     * @param attribute chiave dell'attributo da impostare
     * @param value valore dell'attributo
     */
    public void setMetadataAttribute(SensorAttributeEnum attribute, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(attribute.name(), value);
    }

    /**
     * Recupera un attributo dal metadata utilizzando l'enum SensorAttributeEnum.
     *
     * @param attribute chiave dell'attributo
     * @return valore dell'attributo oppure null se non presente
     */
    public Object getMetadataAttribute(SensorAttributeEnum attribute) {
        if (metadata == null) {
            return null;
        }
        return metadata.get(attribute.name());
    }

    /**
     * Recupera un attributo dal metadata come String.
     *
     * @param attribute chiave dell'attributo
     * @return valore dell'attributo come String oppure null
     */
    public String getMetadataAttributeAsString(SensorAttributeEnum attribute) {
        Object value = getMetadataAttribute(attribute);
        return value != null ? value.toString() : null;
    }

    /**
     * Recupera un attributo dal metadata come Number.
     *
     * @param attribute chiave dell'attributo
     * @return valore dell'attributo come Number oppure null
     */
    public Number getMetadataAttributeAsNumber(SensorAttributeEnum attribute) {
        Object value = getMetadataAttribute(attribute);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
