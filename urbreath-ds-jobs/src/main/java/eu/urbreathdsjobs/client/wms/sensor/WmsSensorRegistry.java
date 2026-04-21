package eu.urbreathdsjobs.client.wms.sensor;

import eu.urbreathdsjobs.client.wms.City;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping WMS request keys to sensor identifiers by city.
 * This registry maintains mappings between WMS keys and their corresponding sensors.
 */
@Service
@Slf4j
public class WmsSensorRegistry {

    private final Map<String, SensorMapping> sensorMappings;

    public WmsSensorRegistry() {
        this.sensorMappings = initializeSensorMappings();
    }

    /**
     * Initialize sensor mappings for all cities and their respective keys.
     * The mappings are based on city and WMS parameter key combinations.
     *
     * @return Map of city-key combinations to sensor identifiers
     */
    private Map<String, SensorMapping> initializeSensorMappings() {
        Map<String, SensorMapping> mappings = new HashMap<>();

        // Madrid sensors
        mappings.put(getSensorKey(City.MADRID, "precipitation"), new SensorMapping(City.MADRID, "precipitation", "MADRID_PRECIP_SENSOR"));
        mappings.put(getSensorKey(City.MADRID, "temperature"), new SensorMapping(City.MADRID, "temperature", "MADRID_TEMP_SENSOR"));
        mappings.put(getSensorKey(City.MADRID, "traffic"), new SensorMapping(City.MADRID, "traffic", "MADRID_TRAFFIC_SENSOR"));

        // Leuven sensors
        mappings.put(getSensorKey(City.LEUVEN, "precipitation"), new SensorMapping(City.LEUVEN, "precipitation", "LEUVEN_PRECIP_SENSOR"));
        mappings.put(getSensorKey(City.LEUVEN, "temperature"), new SensorMapping(City.LEUVEN, "temperature", "LEUVEN_TEMP_SENSOR"));
        mappings.put(getSensorKey(City.LEUVEN, "traffic"), new SensorMapping(City.LEUVEN, "traffic", "LEUVEN_TRAFFIC_SENSOR"));

        // Cluj sensors
        mappings.put(getSensorKey(City.CLUJ, "precipitation"), new SensorMapping(City.CLUJ, "precipitation", "CLUJ_PRECIP_SENSOR"));
        mappings.put(getSensorKey(City.CLUJ, "temperature"), new SensorMapping(City.CLUJ, "temperature", "CLUJ_TEMP_SENSOR"));
        mappings.put(getSensorKey(City.CLUJ, "traffic"), new SensorMapping(City.CLUJ, "traffic", "CLUJ_TRAFFIC_SENSOR"));

        // Tallinn sensors
        mappings.put(getSensorKey(City.TALLIN, "precipitation"), new SensorMapping(City.TALLIN, "precipitation", "TALLIN_PRECIP_SENSOR"));
        mappings.put(getSensorKey(City.TALLIN, "temperature"), new SensorMapping(City.TALLIN, "temperature", "TALLIN_TEMP_SENSOR"));
        mappings.put(getSensorKey(City.TALLIN, "traffic"), new SensorMapping(City.TALLIN, "traffic", "TALLIN_TRAFFIC_SENSOR"));

        log.info("WmsSensorRegistry initialized with {} sensor mappings", mappings.size());
        return mappings;
    }

    /**
     * Get the sensor identifier for a given city and WMS key.
     *
     * @param city the city
     * @param key  the WMS parameter key (e.g., "precipitation", "temperature", "traffic")
     * @return the sensor identifier, or null if no mapping exists
     */
    public String getSensor(City city, String key) {
        if (city == null || key == null) {
            return null;
        }

        String sensorKey = getSensorKey(city, key);
        SensorMapping mapping = sensorMappings.get(sensorKey);

        if (mapping != null) {
            return mapping.getSensorId();
        }

        log.debug("No sensor mapping found for city={}, key={}", city.getName(), key);
        return null;
    }

    /**
     * Generate a composite key for sensor lookup.
     *
     * @param city the city
     * @param key  the WMS parameter key
     * @return composite key in format "CITY_NAME:KEY"
     */
    private String getSensorKey(City city, String key) {
        return city.getName() + ":" + key;
    }

    /**
     * Add or update a sensor mapping at runtime.
     *
     * @param city     the city
     * @param key      the WMS parameter key
     * @param sensorId the sensor identifier
     */
    public void registerSensor(City city, String key, String sensorId) {
        String mapKey = getSensorKey(city, key);
        SensorMapping mapping = new SensorMapping(city, key, sensorId);
        sensorMappings.put(mapKey, mapping);
        log.info("Registered sensor mapping: city={}, key={}, sensorId={}", city.getName(), key, sensorId);
    }

    /**
     * Remove a sensor mapping.
     *
     * @param city the city
     * @param key  the WMS parameter key
     */
    public void unregisterSensor(City city, String key) {
        String mapKey = getSensorKey(city, key);
        if (sensorMappings.remove(mapKey) != null) {
            log.info("Unregistered sensor mapping: city={}, key={}", city.getName(), key);
        }
    }

    /**
     * Get total number of registered sensors.
     *
     * @return count of sensor mappings
     */
    public int getSensorCount() {
        return sensorMappings.size();
    }
}

