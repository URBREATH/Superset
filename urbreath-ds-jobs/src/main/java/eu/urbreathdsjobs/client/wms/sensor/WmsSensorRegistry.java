package eu.urbreathdsjobs.client.wms.sensor;

import eu.urbreathdsjobs.client.wms.City;
import eu.urbreathdsjobs.client.wms.WmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping WMS request keys to sensor identifiers by city.
 * Mappings are loaded dynamically from the {@code wms.sensors} section
 * of the application configuration (application.yaml).
 *
 * <pre>
 * wms:
 *   sensors:
 *     MADRID:
 *       FIC_THRESHOLD_TMIN_URB: 1002
 *       FIC_THRESHOLD_TMAX_URB: 1001
 *       ...
 * </pre>
 */
@Service
@Slf4j
public class WmsSensorRegistry {

    private final WmsProperties wmsProperties;
    private final Map<String, SensorMapping> sensorMappings;

    public WmsSensorRegistry(WmsProperties wmsProperties) {
        this.wmsProperties = wmsProperties;
        this.sensorMappings = initializeSensorMappings();
    }

    /**
     * Builds sensor mappings by reading {@code wms.sensors} from the application properties.
     * Each entry maps a city name to a map of WMS threshold keys and their sensor IDs.
     *
     * @return Map of city-key combinations to sensor identifiers
     */
    private Map<String, SensorMapping> initializeSensorMappings() {
        Map<String, SensorMapping> mappings = new HashMap<>();

        Map<String, Map<String, String>> sensorsConfig = wmsProperties.getSensors();
        if (sensorsConfig == null || sensorsConfig.isEmpty()) {
            log.warn("No sensor mappings found under wms.sensors in configuration");
            return mappings;
        }

        for (Map.Entry<String, Map<String, String>> cityEntry : sensorsConfig.entrySet()) {
            String cityName = cityEntry.getKey();
            City city = resolveCity(cityName);
            if (city == null) {
                log.warn("Unknown city '{}' found in wms.sensors configuration – skipping", cityName);
                continue;
            }

            Map<String, String> keySensorMap = cityEntry.getValue();
            if (keySensorMap == null || keySensorMap.isEmpty()) {
                log.warn("No sensor keys configured for city '{}'", cityName);
                continue;
            }

            for (Map.Entry<String, String> sensorEntry : keySensorMap.entrySet()) {
                String wmsKey  = sensorEntry.getKey();
                String sensorId = sensorEntry.getValue();
                mappings.put(getSensorKey(city, wmsKey), new SensorMapping(city, wmsKey, sensorId));
                log.debug("Loaded sensor mapping: city={}, wmsKey={}, sensorId={}", cityName, wmsKey, sensorId);
            }
        }

        log.info("WmsSensorRegistry initialized with {} sensor mappings", mappings.size());
        return mappings;
    }

    /**
     * Resolves a {@link City} enum value from its string name (case-insensitive).
     *
     * @param cityName the city name as it appears in the configuration
     * @return the matching {@link City}, or {@code null} if not found
     */
    private City resolveCity(String cityName) {
        for (City c : City.values()) {
            if (c.getName().equalsIgnoreCase(cityName)) {
                return c;
            }
        }
        return null;
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

