package eu.urbreathdsjobs.service;

import eu.urbreathdsjobs.dao.CityDao;
import eu.urbreathdsjobs.dao.LocationDao;
import eu.urbreathdsjobs.dao.ParameterDao;
import eu.urbreathdsjobs.dao.SensorDao;
import eu.urbreathdsjobs.model.City;
import eu.urbreathdsjobs.model.Location;
import eu.urbreathdsjobs.model.Parameter;
import eu.urbreathdsjobs.model.Sensor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class FrostSensorPersistenceService {

    private final CityDao cityDao;
    private final ParameterDao parameterDao;
    private final LocationDao locationDao;
    private final SensorDao sensorDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Sensor insertSensor(Parameter parameter, Location location, Sensor sensor, City city) {
        Objects.requireNonNull(parameter, "parameter is required");
        Objects.requireNonNull(location, "location is required");
        Objects.requireNonNull(sensor, "sensor is required");

        String cityName = city != null ? city.getName() : null;
        City resolvedCity = cityDao.findByNameOrFallback(cityName);
        if (resolvedCity == null) {
            throw new IllegalStateException("Unable to resolve city from DB and fallback 'n/a'");
        }

        location.setIdCity(resolvedCity.getIdCity());
        location.setIdCountry(resolvedCity.getIdCountry());
        if (city != null) {
            city.setIdCity(resolvedCity.getIdCity());
            city.setIdCountry(resolvedCity.getIdCountry());
            city.setName(resolvedCity.getName());
        }

        Long existingParameterId = parameterDao.findIdByNameUnitsDisplayName(
                parameter.getName(),
                parameter.getUnits(),
                parameter.getDisplayName());

        if (existingParameterId != null) {
            parameter.setIdParam(existingParameterId);
            log.info("Reusing existing parameter id_param={} for name={}, units={}, displayName={}",
                    existingParameterId,
                    parameter.getName(),
                    parameter.getUnits(),
                    parameter.getDisplayName());
        } else {
            parameterDao.insertParameter(parameter);
        }

        Long existingLocationId = locationDao.findIdByCoordinates(location.getLatitude(), location.getLongitude());
        if (existingLocationId != null) {
            location.setIdLocation(existingLocationId);
            log.info("Reusing existing location id_location={} for latitude={}, longitude={}",
                    existingLocationId,
                    location.getLatitude(),
                    location.getLongitude());
        } else {
            locationDao.insertLocation(location);
        }

        alignSensorReferences(sensor, parameter, location);
        return sensorDao.insertSensor(sensor);
    }

    private void alignSensorReferences(Sensor sensor, Parameter parameter, Location location) {
        if (sensor.getIdParam() == null) {
            sensor.setIdParam(parameter.getIdParam());
        } else if (!sensor.getIdParam().equals(parameter.getIdParam())) {
            throw new IllegalArgumentException("sensor.idParam does not match parameter.idParam");
        }

        if (sensor.getIdLocation() == null) {
            sensor.setIdLocation(location.getIdLocation());
        } else if (!sensor.getIdLocation().equals(location.getIdLocation())) {
            throw new IllegalArgumentException("sensor.idLocation does not match location.idLocation");
        }
    }
}

