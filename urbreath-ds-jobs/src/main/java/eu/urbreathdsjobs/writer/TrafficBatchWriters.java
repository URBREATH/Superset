package eu.urbreathdsjobs.writer;

import eu.urbreathdsjobs.model.TrafficHistogram;
import eu.urbreathdsjobs.model.TrafficMeasurement;
import eu.urbreathdsjobs.service.TrafficPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrafficBatchWriters {

    private final TrafficPersistenceService trafficPersistenceService;

    @Bean
    public ItemWriter<TrafficMeasurement> trafficMeasurementImportWriter() {
        return new ItemWriter<>() {
            @Override
            public void write(Chunk<? extends TrafficMeasurement> chunk) {
                trafficPersistenceService.writeMeasurements(chunk.getItems());
            }
        };
    }

    @Bean
    public ItemWriter<TrafficHistogram> trafficHistogramImportWriter() {
        return new ItemWriter<>() {
            @Override
            public void write(Chunk<? extends TrafficHistogram> chunk) {
                trafficPersistenceService.writeHistograms(chunk.getItems());
            }
        };
    }

}


