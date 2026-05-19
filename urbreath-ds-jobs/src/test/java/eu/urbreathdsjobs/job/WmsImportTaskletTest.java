package eu.urbreathdsjobs.job;

import eu.urbreathdsjobs.client.wms.*;
import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.tasklet.WmsImportTasklet;
import eu.urbreathdsjobs.writer.MeasurementWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("WMS import tasklet tests")
class WmsImportTaskletTest {

    @Test
    @DisplayName("Should process all generated WMS requests")
    void shouldProcessAllGeneratedWmsRequests() throws Exception {
        WmsUrlService wmsUrlService = Mockito.mock(WmsUrlService.class);
        WmsHttpClientService wmsHttpClientService = Mockito.mock(WmsHttpClientService.class);
        WmsResponseHandlerService handlerService = Mockito.mock(WmsResponseHandlerService.class);
        MeasurementWriter measurementWriter = Mockito.mock(MeasurementWriter.class);
        WmsProperties wmsProperties = Mockito.mock(WmsProperties.class);

        when(wmsProperties.getSensors()).thenReturn(Map.of(
                "MADRID", Map.of("FIC_THRESHOLD_TMIN_URB", "1002")
        ));

        WmsImportTasklet tasklet = new WmsImportTasklet(
                wmsUrlService, wmsHttpClientService, handlerService, measurementWriter, wmsProperties);

        WmsRequest request1 = WmsRequest.builder().city(City.MADRID).callType("wind").url("http://example/1").build();
        WmsRequest request2 = WmsRequest.builder().city(City.LEUVEN).callType("precipitation").url("http://example/2").build();
        when(wmsUrlService.generateUrlsForAllCities()).thenReturn(List.of(request1, request2));

        when(wmsHttpClientService.fetch(any(WmsRequest.class)))
                .thenReturn(WmsCallResult.builder().request(request1).statusCode(200).build())
                .thenReturn(WmsCallResult.builder().request(request2).statusCode(200).build());

        when(handlerService.handle(any(WmsCallResult.class)))
                .thenReturn(List.of(new Measurement()))
                .thenReturn(List.of(new Measurement()));

        StepContribution contribution = Mockito.mock(StepContribution.class);
        ChunkContext chunkContext = Mockito.mock(ChunkContext.class);
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(wmsHttpClientService, times(2)).fetch(any(WmsRequest.class));
        verify(handlerService, times(2)).handle(any(WmsCallResult.class));
        verify(measurementWriter, times(1)).deleteAndWriteBySensorIds(anyList(), anyList());
    }
}

