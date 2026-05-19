package eu.urbreathdsjobs.client.frost;

import de.fraunhofer.iosb.ilt.sta.model.*;
import eu.urbreathdsjobs.common.Constants;
import eu.urbreathdsjobs.common.MeasurementAttributeEnum;
import eu.urbreathdsjobs.model.Measurement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("FROST response handler tests")
class FrostResponseHandlerServiceTest {

    @Test
    @DisplayName("Should map FROST observation into measurement and metadata")
    void shouldMapFrostObservationIntoMeasurementAndMetadata() {
        FrostProperties frostProperties = new FrostProperties();
        frostProperties.setIdParam(19862L);

        FrostProperties.DatastreamConfig datastreamConfig = new FrostProperties.DatastreamConfig();
        datastreamConfig.setDatastreamId(10L);
        datastreamConfig.setSensorId(501L);
        datastreamConfig.setCity("MADRID");
        datastreamConfig.setDescription("Temperatura urbana Madrid");
        datastreamConfig.setObservedProperty("temperature");
        datastreamConfig.setMeasureType("ACTUAL");
        datastreamConfig.setSource("FROST");

        Thing thing = new Thing();
        thing.setId(new IdLong(301L));
        thing.setName("Centralina Madrid Centro");

        Sensor sensor = new Sensor();
        sensor.setId(new IdLong(401L));
        sensor.setName("Sensore PT100");

        ObservedProperty observedProperty = new ObservedProperty();
        observedProperty.setId(new IdLong(601L));
        observedProperty.setName("Air temperature");

        Datastream datastream = new Datastream();
        datastream.setId(new IdLong(10L));
        datastream.setName("Madrid Temperature");
        datastream.setThing(thing);
        datastream.setSensor(sensor);
        datastream.setObservedProperty(observedProperty);

        Observation observation = new Observation();
        observation.setId(new IdLong(9001L));
        observation.setDatastream(datastream);
        observation.setResult(23.7);
        observation.setPhenomenonTime(new TimeObject(ZonedDateTime.parse("2026-04-27T08:15:00Z")));
        observation.setResultTime(ZonedDateTime.parse("2026-04-27T08:16:00Z"));
        observation.setParameters(Map.of("quality", "GOOD"));

        FrostResponseHandlerService handlerService = new FrostResponseHandlerService(frostProperties);
        Measurement measurement = handlerService.mapObservation(datastreamConfig, observation);

        assertNotNull(measurement);
        assertEquals(19862L, measurement.getIdParam());
        assertEquals(501L, measurement.getIdSensor());
        assertEquals(23.7, measurement.getVal());
        assertEquals(2026, measurement.getDateFrom().getYear());
        assertEquals(measurement.getDateFrom(), measurement.getDateTo());
        assertEquals("ACTUAL", measurement.getMetadata().get(MeasurementAttributeEnum.MEASURE_TYPE.name()));
        assertEquals("FROST", measurement.getMetadata().get(Constants.MEASUREMENT_SOURCE));
        assertEquals(9001L, measurement.getMetadata().get("FROST_OBSERVATION_ID"));
        assertEquals(10L, measurement.getMetadata().get("FROST_DATASTREAM_ID"));
        assertEquals("Madrid Temperature", measurement.getMetadata().get("FROST_DATASTREAM_NAME"));
        assertEquals(301L, measurement.getMetadata().get("FROST_THING_ID"));
        assertEquals("Centralina Madrid Centro", measurement.getMetadata().get("FROST_THING_NAME"));
        assertEquals(401L, measurement.getMetadata().get("FROST_SENSOR_ID"));
        assertEquals("Sensore PT100", measurement.getMetadata().get("FROST_SENSOR_NAME"));
        assertEquals(601L, measurement.getMetadata().get("FROST_OBSERVED_PROPERTY_ID"));
        assertEquals("Air temperature", measurement.getMetadata().get("FROST_OBSERVED_PROPERTY_NAME"));
        assertEquals("MADRID", measurement.getMetadata().get("FROST_CITY"));
        assertEquals("temperature", measurement.getMetadata().get("FROST_CONFIGURED_OBSERVED_PROPERTY"));
    }
}

