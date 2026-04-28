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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        long stepStartNanos = System.nanoTime();
        List<WmsRequest> requests = wmsUrlService.generateUrlsForAllCities();
        log.info("Starting WMS import step. Generated {} requests.", requests.size());
        int successCount = 0;
        int failedCount = 0;

        List<Measurement> allMeasurements = new ArrayList<>();

        for (WmsRequest request : requests) {
            long startNanos = System.nanoTime();
            try {
                log.debug("Calling WMS endpoint for city={}, callType={}", request.getCity(), request.getCallType());
                WmsCallResult result = wmsHttpClientService.fetch(request);
                List<Measurement> measurements = wmsResponseHandlerService.handle(result);
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                allMeasurements.addAll(measurements);
                successCount++;
                log.info(
                        "WMS request completed for city={}, callType={}, measurements={}, elapsedMs={}",
                        request.getCity(),
                        request.getCallType(),
                        measurements.size(),
                        elapsedMs
                );
            } catch (RuntimeException ex) {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                failedCount++;
                log.warn(
                        "WMS request failed for city={}, callType={}, elapsedMs={}. Cause: {}",
                        request.getCity(),
                        request.getCallType(),
                        elapsedMs,
                        ex.getMessage()
                );
            }
        }

        long wmsPhaseElapsedMs = (System.nanoTime() - stepStartNanos) / 1_000_000;
        log.info("WMS import step completed. total={}, success={}, failed={}, totalMeasurements={}, elapsedMs={}",
                requests.size(), successCount, failedCount, allMeasurements.size(), wmsPhaseElapsedMs);

        long deleteAndWriteElapsedMs = 0;
        if (!allMeasurements.isEmpty()) {
            List<Long> sensorIds = getConfiguredSensorIds();
            log.info("Persisting measurements: deleteAndWriteBySensorIds for sensors={}, count={}", sensorIds, allMeasurements.size());
            long deleteAndWriteStartNanos = System.nanoTime();
            measurementWriter.deleteAndWriteBySensorIds(sensorIds, allMeasurements);
            deleteAndWriteElapsedMs = (System.nanoTime() - deleteAndWriteStartNanos) / 1_000_000;
            log.info("deleteAndWriteBySensorIds completed for sensorsCount={}, count={}, elapsedMs={}",
                    sensorIds.size(), allMeasurements.size(), deleteAndWriteElapsedMs);
        } else {
            log.warn("No measurements collected, skipping deleteAndWrite.");
        }

        long endToEndElapsedMs = (System.nanoTime() - stepStartNanos) / 1_000_000;
        log.info("WMS import step end-to-end completed. total={}, success={}, failed={}, totalMeasurements={}, deleteAndWriteElapsedMs={}, endToEndElapsedMs={}",
                requests.size(), successCount, failedCount, allMeasurements.size(), deleteAndWriteElapsedMs, endToEndElapsedMs);

        return RepeatStatus.FINISHED;
    }

    private List<Long> getConfiguredSensorIds() {
        Map<String, Map<String, String>> sensorsConfig = wmsProperties.getSensors();
        if (sensorsConfig == null || sensorsConfig.isEmpty()) {
            throw new IllegalStateException("Missing WMS sensor configuration under wms.sensors");
        }

        Set<Long> sensorIds = new LinkedHashSet<>();
        for (Map.Entry<String, Map<String, String>> cityEntry : sensorsConfig.entrySet()) {
            Map<String, String> citySensors = cityEntry.getValue();
            if (citySensors == null || citySensors.isEmpty()) {
                continue;
            }

            for (String sensorIdRaw : citySensors.values()) {
                try {
                    sensorIds.add(Long.valueOf(sensorIdRaw));
                } catch (NumberFormatException ex) {
                    throw new IllegalStateException(
                            String.format("Invalid sensor id '%s' in wms.sensors for city '%s'", sensorIdRaw, cityEntry.getKey()),
                            ex
                    );
                }
            }
        }

        if (sensorIds.isEmpty()) {
            throw new IllegalStateException("No sensor ids found in wms.sensors configuration");
        }

        return new ArrayList<>(sensorIds);
    }
}
