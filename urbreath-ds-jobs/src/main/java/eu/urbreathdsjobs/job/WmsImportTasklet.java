package eu.urbreathdsjobs.job;

import eu.urbreathdsjobs.client.wms.WmsCallResult;
import eu.urbreathdsjobs.client.wms.WmsHttpClientService;
import eu.urbreathdsjobs.client.wms.WmsProperties;
import eu.urbreathdsjobs.client.wms.WmsRequest;
import eu.urbreathdsjobs.client.wms.WmsResponseHandlerService;
import eu.urbreathdsjobs.client.wms.WmsUrlService;
import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.writer.MeasurementWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WmsImportTasklet implements Tasklet {

    private final WmsUrlService wmsUrlService;
    private final WmsHttpClientService wmsHttpClientService;
    private final WmsResponseHandlerService wmsResponseHandlerService;
    private final MeasurementWriter measurementWriter;
    private final WmsProperties wmsProperties;

    @Autowired
    public WmsImportTasklet(
            WmsUrlService wmsUrlService,
            WmsHttpClientService wmsHttpClientService,
            WmsResponseHandlerService wmsResponseHandlerService,
            MeasurementWriter measurementWriter,
            WmsProperties wmsProperties
    ) {
        this.wmsUrlService = wmsUrlService;
        this.wmsHttpClientService = wmsHttpClientService;
        this.wmsResponseHandlerService = wmsResponseHandlerService;
        this.measurementWriter = measurementWriter;
        this.wmsProperties = wmsProperties;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        List<WmsRequest> requests = wmsUrlService.generateUrlsForAllCities();
        int successCount = 0;
        int failedCount = 0;

        List<Measurement> allMeasurements = new ArrayList<>();

        for (WmsRequest request : requests) {
            try {
                WmsCallResult result = wmsHttpClientService.fetch(request);
                List<Measurement> measurements = wmsResponseHandlerService.handle(result);
                allMeasurements.addAll(measurements);
                successCount++;
            } catch (RuntimeException ex) {
                failedCount++;
                log.warn(
                        "WMS request failed for city={}, callType={}. Cause: {}",
                        request.getCity(),
                        request.getCallType(),
                        ex.getMessage()
                );
            }
        }

        log.info("WMS import step completed. total={}, success={}, failed={}, totalMeasurements={}",
                requests.size(), successCount, failedCount, allMeasurements.size());

        if (!allMeasurements.isEmpty()) {
            Long idParam = wmsProperties.getIdParam();
            log.info("Persisting measurements: deleteAndWrite for id_param={}, count={}", idParam, allMeasurements.size());
            measurementWriter.deleteAndWrite(idParam, allMeasurements);
        } else {
            log.warn("No measurements collected, skipping deleteAndWrite.");
        }

        return RepeatStatus.FINISHED;
    }
}
