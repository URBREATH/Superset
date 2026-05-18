package eu.urbreathdsjobs.tasklet;

import ch.qos.logback.core.net.SyslogOutputStream;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import eu.urbreathdsjobs.client.frost.FrostClientService;
import eu.urbreathdsjobs.common.Constants;
import eu.urbreathdsjobs.model.City;
import eu.urbreathdsjobs.model.Location;
import eu.urbreathdsjobs.model.Parameter;
import eu.urbreathdsjobs.common.SensorAttributeEnum;
import eu.urbreathdsjobs.reader.SensorReader;
import eu.urbreathdsjobs.writer.SensorWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
public class FrostConfigTasklet implements Tasklet {

    private final FrostClientService frostClientService;
    private final SensorReader sensorReader;
    private final SensorWriter sensorWriter;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {

        int sensorPage = frostClientService.countSensorPages();
        for ( int i = 0; i < sensorPage; i++) {
            List<Sensor> sensors = frostClientService.fetchSensorsPage(i);
            
            for ( Sensor sensor : sensors ){
                Long sensorId = frostClientService.idValue(sensor.getId());
                if (sensorId == null) {
                    log.warn("Skipping sensor because id is missing");
                    continue;
                }
                List<Datastream> datastreams = frostClientService.fetchDatastreamsForSensor(sensorId);

                for (Datastream datastream : datastreams){
                    Id id = datastream.getId();
                    Long idSensor = frostClientService.idValue(id);

                    String unitSymbol = datastream.getUnitOfMeasurement() != null
                        ? datastream.getUnitOfMeasurement().getSymbol()
                        : "N/A";

                    //1. unitSymbol non in lista  →  skip
                    if (!Constants.ACCEPTED_UNIT_SYMBOLS.contains(unitSymbol)) {
                        log.debug("Skipping datastream id={} - unitSymbol='{}' not in accepted list", id, unitSymbol);
                        continue;
                    }

                    //2. sensore già esiste nel DB (SENSOR_ID_EXTERNAL = idSensor)  →  skip
                    if (sensorReader.sensorExists(String.valueOf(idSensor))) {
                        log.debug("Skipping datastream id={} - sensor with SENSOR_ID_EXTERNAL={} already exists", id, idSensor);
                        continue;
                    }


                    Parameter parameter = getParameter(datastream);
                    Location location = getLocation(datastream);
                    City city = getCity(datastream);
                    eu.urbreathdsjobs.model.Sensor urSensor = getSensor(idSensor, parameter, location);
                    sensorWriter.insertSensor(parameter,location, urSensor, city );
                }

                log.debug("Fetched {} datastreams for sensorId={}", datastreams.size(), sensorId);
            }



            log.debug("Fetched sensor page {}/{}: {} sensors", i, sensorPage, sensors.size());
        }



        return RepeatStatus.FINISHED;
    }

    private Location getLocation(Datastream datastream) {
        Location location = new Location();

        GeoJsonObject observedArea = datastream.getObservedArea();
        if (observedArea instanceof Point point) {
            LngLatAlt coordinates = point.getCoordinates();
            if (coordinates != null) {
                // GeoJSON coordinates order: [longitude, latitude]
                location.setLongitude(coordinates.getLongitude());
                location.setLatitude(coordinates.getLatitude());
            }

        }

        return location;
    }

    private Parameter getParameter(Datastream datastream) {
        String description = datastream.getDescription() != null
            ? datastream.getName()
            : "N/A";
        String unitName = datastream.getUnitOfMeasurement() != null
            ? datastream.getUnitOfMeasurement().getName()
            : "N/A";

        String nameFromDescription = description.split("\\s+")[0];

        Parameter parameter = new Parameter();
        parameter.setName(nameFromDescription);
        parameter.setUnits(datastream.getUnitOfMeasurement().getSymbol());
        parameter.setDisplayName(unitName);
        parameter.setDescription(description);

        return parameter;
    }

    private City getCity(Datastream datastream) {
        Object properties = datastream.getProperties();
        if (!(properties instanceof Map<?, ?> propertiesMap)) {
            return null;
        }

        Object pilot = propertiesMap.get("pilot");
        if (pilot == null) {
            return null;
        }

        String cityName = pilot.toString().trim();
        if (cityName.isEmpty()) {
            return null;
        }

        City city = new City();
        city.setName(cityName);
        return city;
    }

    private eu.urbreathdsjobs.model.Sensor getSensor(Long idSensor, Parameter parameter, Location location) {
        Sensor sensor = new Sensor();
        eu.urbreathdsjobs.model.Sensor urSensor = new eu.urbreathdsjobs.model.Sensor();

        urSensor.setLatitude(location.getLatitude());
        urSensor.setLongitude(location.getLongitude());
        urSensor.setName(parameter.getName());
        urSensor.setDisplayName(parameter.getDisplayName());
        urSensor.setMetadataAttribute(SensorAttributeEnum.SENSOR_ID_EXTERNAL, String.valueOf(idSensor));

        return urSensor;
    }


}
