package eu.urbreathdsjobs.job;

import de.fraunhofer.iosb.ilt.sta.model.Observation;
import eu.urbreathdsjobs.client.frost.FrostClientService;
import eu.urbreathdsjobs.client.frost.FrostProperties;
import eu.urbreathdsjobs.dao.SensorDao;
import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.model.Sensor;
import eu.urbreathdsjobs.service.MeasurementPersistenceService;
import eu.urbreathdsjobs.tasklet.FrostImportTasklet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FROST import tasklet tests")
class FrostImportTaskletTest {

    @Test
    @DisplayName("Should build daily measurements and persist them when dry-run is disabled")
    void shouldBuildDailyMeasurementsAndPersistThem() {
        FrostClientService frostClientService = Mockito.mock(FrostClientService.class);
        FrostProperties frostProperties = Mockito.mock(FrostProperties.class);
        SensorDao sensorDao = Mockito.mock(SensorDao.class);
        MeasurementPersistenceService measurementPersistenceService = Mockito.mock(MeasurementPersistenceService.class);

        when(frostProperties.getPageSize()).thenReturn(5);
        when(frostProperties.isFollowPaginationLinks()).thenReturn(true);
        when(frostProperties.getFilter()).thenReturn(null);

        FrostImportTasklet tasklet = new FrostImportTasklet(
                frostClientService,
                frostProperties,
                sensorDao,
                measurementPersistenceService
        );
        ReflectionTestUtils.setField(tasklet, "dryRun", false);

        Sensor sensor = new Sensor();
        sensor.setIdSensor(501L);
        sensor.setIdParam(19862L);
        sensor.setMetadataAttribute(eu.urbreathdsjobs.common.SensorAttributeEnum.SENSOR_ID_EXTERNAL, 10L);
        when(sensorDao.findSensorsWithExternalId()).thenReturn(List.of(sensor));

        List<Observation> page0 = List.of(
                observation(1.0, ZonedDateTime.of(2024, 1, 1, 1, 0, 0, 0, ZoneOffset.UTC)),
                observation(3.0, ZonedDateTime.of(2024, 1, 1, 2, 0, 0, 0, ZoneOffset.UTC)),
                observation(5.0, ZonedDateTime.of(2024, 1, 1, 3, 0, 0, 0, ZoneOffset.UTC)),
                observation(10.0, ZonedDateTime.of(2024, 1, 2, 1, 0, 0, 0, ZoneOffset.UTC)),
                observation(20.0, ZonedDateTime.of(2024, 1, 2, 2, 0, 0, 0, ZoneOffset.UTC))
        );
        when(frostClientService.fetchObservationsPage(any(), anyInt()))
                .thenReturn(page0)
                .thenReturn(List.of());

        StepContribution contribution = Mockito.mock(StepContribution.class);
        ChunkContext chunkContext = Mockito.mock(ChunkContext.class);
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Measurement>> captor = ArgumentCaptor.forClass((Class<List<Measurement>>) (Class<?>) List.class);
        verify(measurementPersistenceService, times(1)).writeMeasurementsAndUpdateLastObservationDate(eq(501L), eq("2024-01-02T02:00:00Z"), captor.capture());
        verify(measurementPersistenceService, never()).deleteAndWriteBySensorIds(any(), any());

        List<Measurement> measurements = captor.getValue();
        assertEquals(2, measurements.size());

        Measurement day1 = measurements.getFirst();
        assertNotNull(day1.getIdMeasure());
        assertEquals(501L, day1.getIdSensor());
        assertEquals(19862L, day1.getIdParam());
        assertEquals("1day", day1.getPeriod());
        assertEquals(ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toLocalDateTime(), day1.getDateFrom());
        assertEquals(ZonedDateTime.of(2024, 1, 1, 23, 59, 59, 999999999, ZoneOffset.UTC).toLocalDateTime(), day1.getDateTo());
        assertEquals(1.0, day1.getMin());
        assertEquals(5.0, day1.getMax());
        assertEquals(3.0, day1.getAvg());
        assertEquals(3.0, day1.getVal());
        assertEquals(3.0, day1.getMedian());
        assertEquals("FROST_SERVER", day1.getMetadata().get("MEASURE_TYPE"));
        assertTrue(day1.getQ02() >= day1.getMin() && day1.getQ02() <= day1.getMax());
        assertTrue(day1.getQ24() >= day1.getMin() && day1.getQ24() <= day1.getMax());
        assertTrue(day1.getQ75() >= day1.getMin() && day1.getQ75() <= day1.getMax());
        assertTrue(day1.getQ98() >= day1.getMin() && day1.getQ98() <= day1.getMax());
        assertTrue(day1.getSd() >= 0.0);
    }

    @Test
    @DisplayName("Should log-only when dry-run is enabled")
    void shouldLogOnlyWhenDryRunIsEnabled() {
        FrostClientService frostClientService = Mockito.mock(FrostClientService.class);
        FrostProperties frostProperties = Mockito.mock(FrostProperties.class);
        SensorDao sensorDao = Mockito.mock(SensorDao.class);
        MeasurementPersistenceService measurementPersistenceService = Mockito.mock(MeasurementPersistenceService.class);

        when(frostProperties.getPageSize()).thenReturn(5);
        when(frostProperties.isFollowPaginationLinks()).thenReturn(true);
        when(frostProperties.getFilter()).thenReturn(null);

        FrostImportTasklet tasklet = new FrostImportTasklet(
                frostClientService,
                frostProperties,
                sensorDao,
                measurementPersistenceService
        );
        ReflectionTestUtils.setField(tasklet, "dryRun", true);

        Sensor sensor = new Sensor();
        sensor.setIdSensor(777L);
        sensor.setIdParam(19862L);
        sensor.setMetadataAttribute(eu.urbreathdsjobs.common.SensorAttributeEnum.SENSOR_ID_EXTERNAL, 11L);
        when(sensorDao.findSensorsWithExternalId()).thenReturn(List.of(sensor));

        when(frostClientService.fetchObservationsPage(any(), anyInt()))
                .thenReturn(List.of(observation(42.0, ZonedDateTime.of(2024, 2, 1, 10, 0, 0, 0, ZoneOffset.UTC))))
                .thenReturn(List.of());

        RepeatStatus status = tasklet.execute(Mockito.mock(StepContribution.class), Mockito.mock(ChunkContext.class));

        assertEquals(RepeatStatus.FINISHED, status);
        verify(measurementPersistenceService, never()).writeMeasurements(any());
        verify(measurementPersistenceService, never()).writeMeasurementsAndUpdateLastObservationDate(any(), any(), any());
    }

    private Observation observation(double value, ZonedDateTime phenomenonTime) {
        return new Observation(value, phenomenonTime);
    }
}

