package eu.urbreathdsjobs.job;

import eu.urbreathdsjobs.client.wms.WmsCallResult;
import eu.urbreathdsjobs.client.wms.WmsHttpClientService;
import eu.urbreathdsjobs.client.wms.WmsRequest;
import eu.urbreathdsjobs.client.wms.WmsUrlService;
import eu.urbreathdsjobs.launcher.App;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;

@SpringBootTest(
        classes = App.class,
        properties = "app.scheduler.wms.cron=0 0 0 1 1 *"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WmsImportJobTest {

    @SpyBean
    private JobWSMScheduler jobWSMScheduler;

    @SpyBean
    private WmsUrlService wmsUrlService;

    @MockBean
    private WmsHttpClientService wmsHttpClientService;

    @MockBean
    private JobExplorer jobExplorer;

    @Test
    void schedulerShouldLaunchWmsImportJobAndFetchAllGeneratedRequests() throws Exception {
        when(wmsHttpClientService.fetch(any(WmsRequest.class))).thenAnswer(invocation -> {
            WmsRequest request = invocation.getArgument(0);
            return WmsCallResult.builder()
                    .request(request)
                    .statusCode(200)
                    .build();
        });

        List<WmsRequest> expectedRequests = wmsUrlService.generateUrlsForAllCities();

        verify(jobWSMScheduler, timeout(45000).atLeastOnce()).runWmsImportJob();

        ArgumentCaptor<WmsRequest> requestCaptor = ArgumentCaptor.forClass(WmsRequest.class);
        verify(wmsHttpClientService, timeout(45000).atLeast(expectedRequests.size()))
                .fetch(requestCaptor.capture());

        assertTrue(new HashSet<>(requestCaptor.getAllValues()).containsAll(expectedRequests));
    }
}
