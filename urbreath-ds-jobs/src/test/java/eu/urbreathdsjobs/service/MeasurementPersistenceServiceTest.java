package eu.urbreathdsjobs.service;

import eu.urbreathdsjobs.dao.MeasurementDao;
import eu.urbreathdsjobs.model.Measurement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MeasurementPersistenceService tests")
class MeasurementPersistenceServiceTest {

    @Mock
    private MeasurementDao measurementDao;

    @InjectMocks
    private MeasurementPersistenceService measurementPersistenceService;

    @Test
    void deleteByIdParamDelegatesToDao() {
        Long idParam = 101L;

        measurementPersistenceService.deleteByIdParam(idParam);

        verify(measurementDao, times(1)).deleteByIdParam(idParam);
    }

    @Test
    void deleteBySensorIdsSkipsWhenNullOrEmpty() {
        measurementPersistenceService.deleteBySensorIds(null);
        measurementPersistenceService.deleteBySensorIds(List.of());

        verify(measurementDao, never()).deleteBySensorIds(anyList());
    }

    @Test
    void deleteBySensorIdsDelegatesWhenPresent() {
        List<Long> sensorIds = List.of(10L, 20L, 20L);

        measurementPersistenceService.deleteBySensorIds(sensorIds);

        verify(measurementDao, times(1)).deleteBySensorIds(sensorIds);
    }

    @Test
    void writeMeasurementsDelegatesToDao() {
        List<Measurement> items = List.of(new Measurement(), new Measurement());

        measurementPersistenceService.writeMeasurements(items);

        verify(measurementDao, times(1)).batchInsert(items);
    }

    @Test
    void deleteAndWriteBySensorIdsChunksAt500() {
        List<Long> sensorIds = List.of(1L, 2L);
        List<Measurement> measurements = new ArrayList<>();
        for (int i = 0; i < 1200; i++) {
            measurements.add(new Measurement());
        }

        measurementPersistenceService.deleteAndWriteBySensorIds(sensorIds, measurements);

        verify(measurementDao, times(1)).deleteBySensorIds(eq(sensorIds));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Measurement>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(measurementDao, times(3)).batchInsert(captor.capture());

        List<List<Measurement>> batches = captor.getAllValues();
        assertEquals(3, batches.size());
        assertEquals(500, batches.get(0).size());
        assertEquals(500, batches.get(1).size());
        assertEquals(200, batches.get(2).size());
    }
}

