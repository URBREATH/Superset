package eu.urbreathdsjobs.tasklet;

import ch.qos.logback.core.net.SyslogOutputStream;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import eu.urbreathdsjobs.client.frost.FrostClientService;
import eu.urbreathdsjobs.client.frost.FrostProperties;
import eu.urbreathdsjobs.client.frost.FrostResponseHandlerService;
import eu.urbreathdsjobs.common.Constants;
import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.model.Parameter;
import eu.urbreathdsjobs.reader.SensorReader;
import eu.urbreathdsjobs.writer.MeasurementWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
public class FrostConfigTasklet implements Tasklet {

    private final FrostClientService frostClientService;
    private final FrostResponseHandlerService frostResponseHandlerService;
    private final MeasurementWriter measurementWriter;
    private final FrostProperties frostProperties;
    private final SensorReader sensorReader;

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


                }

                log.debug("Fetched {} datastreams for sensorId={}", datastreams.size(), sensorId);
            }



            log.debug("Fetched sensor page {}/{}: {} sensors", i, sensorPage, sensors.size());
        }



        return RepeatStatus.FINISHED;
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
}
