package eu.urbreathdsjobs.job;

import eu.urbreathdsjobs.client.wms.City;
import eu.urbreathdsjobs.client.wms.WmsCallResult;
import eu.urbreathdsjobs.client.wms.WmsHttpClientService;
import eu.urbreathdsjobs.client.wms.WmsRequest;
import eu.urbreathdsjobs.client.wms.WmsResponseHandlerService;
import eu.urbreathdsjobs.client.wms.WmsUrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("WMS import tasklet tests")
class WmsImportTaskletTest {

    @Test
    @DisplayName("Should process all generated WMS requests")
    void shouldProcessAllGeneratedWmsRequests() {
        WmsUrlService wmsUrlService = Mockito.mock(WmsUrlService.class);
        WmsHttpClientService wmsHttpClientService = Mockito.mock(WmsHttpClientService.class);
        WmsResponseHandlerService handlerService = Mockito.mock(WmsResponseHandlerService.class);

        WmsImportTasklet tasklet = new WmsImportTasklet(wmsUrlService, wmsHttpClientService, handlerService);

        WmsRequest request1 = WmsRequest.builder().city(City.MADRID).callType("wind").url("http://example/1").build();
        WmsRequest request2 = WmsRequest.builder().city(City.LEUVEN).callType("precipitation").url("http://example/2").build();
        when(wmsUrlService.generateUrlsForAllCities()).thenReturn(List.of(request1, request2));

        when(wmsHttpClientService.fetch(any(WmsRequest.class)))
                .thenReturn(WmsCallResult.builder().request(request1).statusCode(200).build())
                .thenReturn(WmsCallResult.builder().request(request2).statusCode(200).build());

        StepContribution contribution = Mockito.mock(StepContribution.class);
        ChunkContext chunkContext = Mockito.mock(ChunkContext.class);
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(wmsHttpClientService, times(2)).fetch(any(WmsRequest.class));
        verify(handlerService, times(2)).handle(any(WmsCallResult.class));
    }
}


