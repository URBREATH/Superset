package eu.urbreathdsjobs.job;

import de.fraunhofer.iosb.ilt.sta.model.Observation;
import eu.urbreathdsjobs.client.frost.FrostClientService;
import eu.urbreathdsjobs.client.frost.FrostProperties;
import eu.urbreathdsjobs.client.frost.FrostResponseHandlerService;
import eu.urbreathdsjobs.model.Measurement;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FROST import tasklet tests")
class FrostImportTaskletTest {

    @Test
    @DisplayName("Should process all configured FROST datastreams")
    void shouldProcessAllConfiguredFrostDatastreams() throws Exception {
        FrostClientService frostClientService = Mockito.mock(FrostClientService.class);
        FrostResponseHandlerService handlerService = Mockito.mock(FrostResponseHandlerService.class);
        MeasurementWriter measurementWriter = Mockito.mock(MeasurementWriter.class);
        FrostProperties frostProperties = Mockito.mock(FrostProperties.class);

        when(frostProperties.getIdParam()).thenReturn(19862L);

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
        when(frostClientService.fetchObservations(any(FrostProperties.DatastreamConfig.class)))
                .thenReturn(List.of(new Observation()))
                .thenReturn(List.of(new Observation()));
        when(handlerService.handle(any(FrostProperties.DatastreamConfig.class), any()))
                .thenReturn(List.of(new Measurement()))
                .thenReturn(List.of(new Measurement()));

        StepContribution contribution = Mockito.mock(StepContribution.class);
        ChunkContext chunkContext = Mockito.mock(ChunkContext.class);
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(frostClientService, times(2)).fetchObservations(any(FrostProperties.DatastreamConfig.class));
        verify(handlerService, times(2)).handle(any(FrostProperties.DatastreamConfig.class), any());
        verify(measurementWriter, times(1)).deleteAndWrite(any(), any());
    }
}

