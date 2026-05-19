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

    @Override
    public void write(Chunk<? extends Measurement> chunk) {
        measurementPersistenceService.writeMeasurements(chunk.getItems());
    }
    
}
