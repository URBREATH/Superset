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
        int totalMeasurements = 0;
        int successCount = 0;
        int failedCount = 0;

        // Delete once at the beginning
        //measurementWriter.deleteByIdParam(frostProperties.getIdParam());

        for (FrostProperties.DatastreamConfig datastreamConfig : datastreamConfigs) {
            try {
                int pageIndex = 0;
                int pagesMeasurements = 0;

                while (true) {
                    List<Observation> observationsPage = frostClientService.fetchObservationsPage(datastreamConfig, pageIndex);

                    if (observationsPage.isEmpty()) {
                        log.debug("End of pages for datastreamId={} at pageIndex={}", datastreamConfig.getDatastreamId(), pageIndex);
                        break;
                    }

                    List<Measurement> measurements = frostResponseHandlerService.handle(datastreamConfig, observationsPage);
                    if (!measurements.isEmpty()) {
                        //measurementWriter.write(new org.springframework.batch.item.Chunk<>(measurements));
                        totalMeasurements += measurements.size();
                        pagesMeasurements += measurements.size();
                    }

                    log.debug(
                            "FROST config page {} for datastreamId={}: {} observations -> {} measurements",
                            pageIndex,
                            datastreamConfig.getDatastreamId(),
                            observationsPage.size(),
                            measurements.size()
                    );

                    // Stop pagination if followPaginationLinks is disabled
                    if (!frostProperties.isFollowPaginationLinks()) {
                        log.debug("followPaginationLinks is disabled, stopping pagination for datastreamId={}", datastreamConfig.getDatastreamId());
                        break;
                    }

                    // Move to next page
                    pageIndex++;
                }

                log.info(
                        "FROST config completed for datastreamId={}, city={}, pages={}, measurements={}",
                        datastreamConfig.getDatastreamId(),
                        datastreamConfig.getCity(),
                        pageIndex,
                        pagesMeasurements
                );
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
                totalMeasurements
        );

        return RepeatStatus.FINISHED;
    }
}


