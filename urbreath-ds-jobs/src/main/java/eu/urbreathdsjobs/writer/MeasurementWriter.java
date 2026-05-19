package eu.urbreathdsjobs.writer;

import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.service.MeasurementPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class MeasurementWriter implements ItemWriter<Measurement> {

    private final MeasurementPersistenceService measurementPersistenceService;

    public void deleteByIdParam(Long idParam) {
        measurementPersistenceService.deleteByIdParam(idParam);
    }

    public void deleteBySensorIds(List<Long> sensorIds) {
        measurementPersistenceService.deleteBySensorIds(sensorIds);
    }

    @Transactional
    public void deleteAndWriteBySensorIds(List<Long> sensorIds, List<Measurement> measurements) {
        measurementPersistenceService.deleteAndWriteBySensorIds(sensorIds, measurements);
    }

    @Override
    public void write(Chunk<? extends Measurement> chunk) {
        measurementPersistenceService.writeMeasurements(chunk.getItems());
    }
    
}
