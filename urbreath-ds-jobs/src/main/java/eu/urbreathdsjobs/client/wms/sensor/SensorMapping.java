package eu.urbreathdsjobs.client.wms.sensor;

import eu.urbreathdsjobs.client.wms.City;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a mapping between a WMS parameter key and a sensor identifier for a specific city.
 */
@Data
@AllArgsConstructor
public class SensorMapping {
    private City city;
    private String parameterKey;
    private String sensorId;
}

