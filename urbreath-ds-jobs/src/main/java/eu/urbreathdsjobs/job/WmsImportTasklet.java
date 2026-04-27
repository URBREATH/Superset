package eu.urbreathdsjobs.job;

import eu.urbreathdsjobs.client.wms.City;
import eu.urbreathdsjobs.client.wms.WmsCallResult;
import eu.urbreathdsjobs.client.wms.WmsHttpClientService;
import eu.urbreathdsjobs.client.wms.WmsRequest;
import eu.urbreathdsjobs.client.wms.WmsResponseHandlerService;
import eu.urbreathdsjobs.client.wms.WmsUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WmsImportTasklet implements Tasklet {

    private static final long EXPECTED_REQUESTS_PER_CITY = 4L;

    private final WmsUrlService wmsUrlService;
    private final WmsHttpClientService wmsHttpClientService;
    private final WmsResponseHandlerService wmsResponseHandlerService;

    @Autowired
    public WmsImportTasklet(
            WmsUrlService wmsUrlService,
            WmsHttpClientService wmsHttpClientService,
            WmsResponseHandlerService wmsResponseHandlerService
    ) {
        this.wmsUrlService = wmsUrlService;
        this.wmsHttpClientService = wmsHttpClientService;
        this.wmsResponseHandlerService = wmsResponseHandlerService;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        List<WmsRequest> requests = wmsUrlService.generateUrlsForAllCities();
        Map<City, Long> requestsByCity = requests.stream()
                .filter(request -> request.getCity() != null)
                .collect(Collectors.groupingBy(WmsRequest::getCity, Collectors.counting()));

        for (City city : City.values()) {
            long generatedForCity = requestsByCity.getOrDefault(city, 0L);
            if (generatedForCity != EXPECTED_REQUESTS_PER_CITY) {
                log.warn(
                        "Unexpected number of generated WMS requests for city={}. expected={}, actual={}",
                        city,
                        EXPECTED_REQUESTS_PER_CITY,
                        generatedForCity
                );
            }
        }

        int successCount = 0;
        int failedCount = 0;

        for (WmsRequest request : requests) {
            try {
                WmsCallResult result = wmsHttpClientService.fetch(request);
                wmsResponseHandlerService.handle(result);
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

        log.info("WMS import step completed. total={}, success={}, failed={}", requests.size(), successCount, failedCount);
        return RepeatStatus.FINISHED;
    }
}


