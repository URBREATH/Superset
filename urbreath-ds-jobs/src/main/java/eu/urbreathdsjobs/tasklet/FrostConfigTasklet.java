package eu.urbreathdsjobs.tasklet;

import de.fraunhofer.iosb.ilt.sta.model.Observation;
import eu.urbreathdsjobs.client.frost.FrostClientService;
import eu.urbreathdsjobs.client.frost.FrostProperties;
import eu.urbreathdsjobs.client.frost.FrostResponseHandlerService;
import eu.urbreathdsjobs.model.Measurement;
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

import java.util.ArrayList;
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

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        List<FrostProperties.DatastreamConfig> datastreamConfigs = frostClientService.getConfiguredDatastreams();
        List<Measurement> allMeasurements = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (FrostProperties.DatastreamConfig datastreamConfig : datastreamConfigs) {
            try {
                List<Observation> observations = frostClientService.fetchObservations(datastreamConfig);
                List<Measurement> measurements = frostResponseHandlerService.handle(datastreamConfig, observations);
                allMeasurements.addAll(measurements);
                successCount++;
            } catch (RuntimeException ex) {
                failedCount++;
                log.warn(
                        "FROST config failed for datastreamId={}, city={}, cause={}",
                        datastreamConfig.getDatastreamId(),
                        datastreamConfig.getCity(),
                        ex.getMessage()
                );
            }
        }

        log.info(
                "FROST config completed. configuredDatastreams={}, success={}, failed={}, totalMeasurements={}",
                datastreamConfigs.size(),
                successCount,
                failedCount,
                allMeasurements.size()
        );

        if (!allMeasurements.isEmpty()) {
            measurementWriter.deleteAndWrite(frostProperties.getIdParam(), allMeasurements);
        } else {
            log.warn("No FROST config measurements collected, skipping deleteAndWrite.");
        }

        return RepeatStatus.FINISHED;
    }
}


