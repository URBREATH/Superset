package eu.urbreathdsjobs.job;

import de.fraunhofer.iosb.ilt.sta.model.Observation;
import eu.urbreathdsjobs.client.frost.FrostClientService;
import eu.urbreathdsjobs.client.frost.FrostProperties;
import eu.urbreathdsjobs.client.frost.FrostResponseHandlerService;
import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.tasklet.FrostImportTasklet;
import eu.urbreathdsjobs.writer.MeasurementWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FROST import tasklet tests")
class FrostImportTaskletTest {

    @Test
    @DisplayName("Should process all configured FROST datastreams with pagination")
    void shouldProcessAllConfiguredFrostDatastreamsWithPagination() throws Exception {
        FrostClientService frostClientService = Mockito.mock(FrostClientService.class);
        FrostResponseHandlerService handlerService = Mockito.mock(FrostResponseHandlerService.class);
        MeasurementWriter measurementWriter = Mockito.mock(MeasurementWriter.class);
        FrostProperties frostProperties = Mockito.mock(FrostProperties.class);

        when(frostProperties.getIdParam()).thenReturn(19862L);
        when(frostProperties.isFollowPaginationLinks()).thenReturn(true);

        FrostImportTasklet tasklet = new FrostImportTasklet(
                frostClientService,
                handlerService,
                measurementWriter,
                frostProperties
        );

        FrostProperties.DatastreamConfig config1 = new FrostProperties.DatastreamConfig();
        config1.setDatastreamId(10L);
        config1.setSensorId(501L);
        FrostProperties.DatastreamConfig config2 = new FrostProperties.DatastreamConfig();
        config2.setDatastreamId(20L);
        config2.setSensorId(502L);

        when(frostClientService.getConfiguredDatastreams()).thenReturn(List.of(config1, config2));

        // Config 1: 2 pages
        Observation obs1 = new Observation();
        Observation obs2 = new Observation();
        Observation obs3 = new Observation();

        when(frostClientService.fetchObservationsPage(any(), anyInt()))
                .thenReturn(List.of(obs1, obs2))  // page 0 for config1
                .thenReturn(List.of(obs3))        // page 1 for config1
                .thenReturn(List.of())             // page 2 for config1 (empty)
                .thenReturn(List.of(new Observation())) // page 0 for config2
                .thenReturn(List.of());             // page 1 for config2 (empty)

        Measurement m1 = new Measurement();
        Measurement m2 = new Measurement();
        Measurement m3 = new Measurement();

        when(handlerService.handle(any(FrostProperties.DatastreamConfig.class), any()))
                .thenReturn(List.of(m1))
                .thenReturn(List.of(m2))
                .thenReturn(List.of(m3));

        StepContribution contribution = Mockito.mock(StepContribution.class);
        ChunkContext chunkContext = Mockito.mock(ChunkContext.class);
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);

        // Verify delete called once at the beginning
        verify(measurementWriter, times(1)).deleteByIdParam(19862L);

        // Verify write called 3 times (one for each page)
        verify(measurementWriter, times(3)).write(any());

        // Verify fetchObservationsPage called for all pages (including empty ones to detect end)
        // Config1: pages 0, 1, 2 (empty)
        // Config2: pages 0, 1 (empty)
        // Total: 5 calls
        verify(frostClientService, times(5)).fetchObservationsPage(any(FrostProperties.DatastreamConfig.class), anyInt());
    }
}

