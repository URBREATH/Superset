package eu.urbreathdsjobs.client.frost;

import de.fraunhofer.iosb.ilt.sta.model.*;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import eu.urbreathdsjobs.launcher.App;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(
        classes = App.class,
        properties = {
                "spring.batch.job.enabled=false",
                "spring.task.scheduling.enabled=false",
                "app.scheduler.wms.enabled=false",
                "app.scheduler.import.traffic.enabled=false",
                "app.scheduler.import.temperature.enabled=false",
                "app.scheduler.import.precipitation.enabled=false",
                "app.scheduler.frost.config.enabled=false",
                "app.scheduler.frost.import.enabled=false"
        }
)
@DisplayName("FROST live client tests")
@EnabledIfSystemProperty(named = "runLiveFrostTests", matches = "true")
@Slf4j
class FrostClientServiceLiveTest {

    private static final String DEFAULT_BASE_URL = "https://frost-dev.urbreath.tech/FROST-Server/v1.1";

    @Autowired(required = false)
    private FrostClientService frostClientService;

    @Autowired(required = false)
    private FrostProperties frostProperties;

    @Autowired(required = false)
    private JobLauncher jobLauncher;

    @Autowired(required = false)
    @Qualifier("frostImportJob")
    private Job frostImportJob;

    @Test
    @DisplayName("Should query at least one datastream with real HTTP calls")
    void shouldQueryDatastreamsWithRealHttpCalls() throws Exception {
        SensorThingsService sensorThingsService = new SensorThingsService(URI.create(resolveBaseUrl()));

        EntityList<Datastream> entityList = sensorThingsService.datastreams().query().top(1).list();
        List<Datastream> datastreams = toList(entityList.fullIterator());

        assertNotNull(datastreams);
        assertTrue(datastreams.size() <= 1);
    }

    @Test
    @DisplayName("Should fetch observations through real FrostClientService")
    void shouldFetchObservationsThroughRealClientService() throws Exception {
        log.info("[LIVE] Starting FROST live workflow test");
        SensorThingsService sensorThingsService = new SensorThingsService(URI.create(resolveBaseUrl()));

        long connectivityStart = System.nanoTime();
        EntityList<Thing> list = sensorThingsService.things().query().top(1).list();// Test preliminare per verificare la connessione al server FROST
        EntityList<Sensor> list5 = sensorThingsService.sensors().query().top(1).list(); // Test preliminare per verificare la connessione al server FROST
        //EntityList<Actuator> list2 = sensorThingsService.actuators().query().top(1).list();// Test preliminare per verificare la connessione al server FROST
        EntityList<ObservedProperty> list1 = sensorThingsService.observedProperties().query().top(1).list(); // Test preliminare per verificare la connessione al server FROST
        EntityList<Observation> list3 = sensorThingsService.observations().query().top(1).list();// Test preliminare per verificare la connessione al server FROST
        EntityList<FeatureOfInterest> list4 = sensorThingsService.featuresOfInterest().query().top(1).list();// Test preliminare per verificare la connessione al server FROST
        log.info("[LIVE] Connectivity probes completed in {} ms", (System.nanoTime() - connectivityStart) / 1_000_000);


        EntityList<Datastream> datastreamEntityList = sensorThingsService.datastreams().query().top(1).list();
        List<Datastream> datastreams = toList(datastreamEntityList.fullIterator());
        List<ObservedProperty> observeProperties = toList(list1.fullIterator());

        assumeTrue(!datastreams.isEmpty(), "Nessun datastream disponibile sul server FROST.");
        Datastream firstDatastream = datastreams.get(0);
        ObservedProperty observerdProperty = observeProperties.get(0);
        Long datastreamId = Long.valueOf(firstDatastream.getId().getValue().toString());
        Long observationId = Long.valueOf(firstDatastream.getId().getValue().toString());

        FrostProperties frostProperties = new FrostProperties();
        frostProperties.setPageSize(300);
        frostProperties.setFollowPaginationLinks(false);
        //frostProperties.setOrderBy("phenomenonTime desc");
        // order by (oltre a phenomenonTime)
        //frostProperties.setOrderBy("resultTime desc");
        // oppure
        //frostProperties.setOrderBy("id desc");
        //frostProperties.setExpand("Datastream($expand=Thing,Sensor,ObservedProperty)");
//        significa: per ogni Observation, includi anche:
//        il suo Datastream
//        e dentro il Datastream includi anche Thing, Sensor, ObservedProperty

        frostProperties.setExpand("");

        FrostProperties.DatastreamConfig datastreamConfig = new FrostProperties.DatastreamConfig();
        datastreamConfig.setDatastreamId(1L);
        //datastreamConfig.setSensorId(resolveSensorId());
        datastreamConfig.setSensorId(1L);
        datastreamConfig.setFilter("result ne null");

//        // valori numerici
//        datastreamConfig.setFilter("result gt 0");
//        datastreamConfig.setFilter("result ge 10 and result le 35");
//
//        // tempo di osservazione (range)
//        datastreamConfig.setFilter("phenomenonTime ge 2026-04-01T00:00:00Z and phenomenonTime lt 2026-05-01T00:00:00Z");
//
//        // tempo di registrazione
//        datastreamConfig.setFilter("resultTime ge 2026-04-28T00:00:00Z");
//
//        // quality / validity (if present in your dataset)
//        datastreamConfig.setFilter("resultQuality ne null");
//        datastreamConfig.setFilter("validTime ne null");
//
//        // feature of interest
//        datastreamConfig.setFilter("FeatureOfInterest/id eq 1");
//
//        // combinato
//        datastreamConfig.setFilter("result ne null and result gt 0 and phenomenonTime ge 2026-04-01T00:00:00Z");
//
//        //datastreamConfig.setObservedProperty(observerdProperty.getName());


        FrostClientService frostClientService = new FrostClientService(sensorThingsService, frostProperties);

        log.info("[LIVE] Calling fetchObservations for datastreamId={}, pageSize={}, followPaginationLinks={}",
                datastreamConfig.getDatastreamId(),
                frostProperties.getPageSize(),
                frostProperties.isFollowPaginationLinks());
        long fetchObservationsStart = System.nanoTime();
        List<Observation> observations = frostClientService.fetchObservations(datastreamConfig);
        log.info("[LIVE] fetchObservations returned {} rows in {} ms", observations.size(), (System.nanoTime() - fetchObservationsStart) / 1_000_000);

        log.info("[LIVE] Calling fetchObservationsPage(pageIndex=0)");
        long firstPageStart = System.nanoTime();
        List<Observation> firstPage = frostClientService.fetchObservationsPage(datastreamConfig, 0);
        log.info("[LIVE] fetchObservationsPage(0) returned {} rows in {} ms", firstPage.size(), (System.nanoTime() - firstPageStart) / 1_000_000);

        log.info("[LIVE] Calling fetchObservationsPage(pageIndex=1)");
        long secondPageStart = System.nanoTime();
        List<Observation> secondPage = frostClientService.fetchObservationsPage(datastreamConfig, 1);
        log.info("[LIVE] fetchObservationsPage(1) returned {} rows in {} ms", secondPage.size(), (System.nanoTime() - secondPageStart) / 1_000_000);

        long countThingPagesStart = System.nanoTime();
        int thingPages = frostClientService.countThingPages();
        log.info("[LIVE] countThingPages returned {} in {} ms", thingPages, (System.nanoTime() - countThingPagesStart) / 1_000_000);

        long countSensorPagesStart = System.nanoTime();
        int sensorPages = frostClientService.countSensorPages();
        log.info("[LIVE] countSensorPages returned {} in {} ms", sensorPages, (System.nanoTime() - countSensorPagesStart) / 1_000_000);

        long countObservedPropertyPagesStart = System.nanoTime();
        int observedPropertyPages = frostClientService.countObservedPropertyPages();
        log.info("[LIVE] countObservedPropertyPages returned {} in {} ms", observedPropertyPages, (System.nanoTime() - countObservedPropertyPagesStart) / 1_000_000);

        long countObservationPagesStart = System.nanoTime();
        int observationPages = frostClientService.countObservationPages(datastreamConfig);
        log.info("[LIVE] countObservationPages returned {} in {} ms", observationPages, (System.nanoTime() - countObservationPagesStart) / 1_000_000);

        long countFeatureOfInterestPagesStart = System.nanoTime();
        int featureOfInterestPages = frostClientService.countFeatureOfInterestPages();
        log.info("[LIVE] countFeatureOfInterestPages returned {} in {} ms", featureOfInterestPages, (System.nanoTime() - countFeatureOfInterestPagesStart) / 1_000_000);

        assertNotNull(observations);
        assertNotNull(firstPage);
        assertNotNull(secondPage);
        assertTrue(observations.size() <= 300);
        assertTrue(firstPage.size() <= 300);
        assertTrue(secondPage.size() <= 300);
        assertEquals(firstPage.size(), observations.size());

        if (!firstPage.isEmpty() && !secondPage.isEmpty()) {
            assertTrue(!firstPage.get(0).getId().equals(secondPage.get(0).getId()));
        }
    }

    private String resolveBaseUrl() {
        String value = System.getProperty("frost.base-url", DEFAULT_BASE_URL);
        assumeTrue(value != null && !value.isBlank(), "Base URL FROST non configurato.");
        return value;
    }

    private Long resolveSensorId() {
        String raw = System.getProperty("frost.live.sensor-id", "1");
        return Long.valueOf(raw);
    }

    private <T> List<T> toList(Iterator<T> iterator) {
        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    @Test
    @DisplayName("Should execute FrostImportTasklet end-to-end with real FROST server (paginated flow)")
    void shouldExecuteFrostImportTaskletEndToEnd() throws Exception {
        log.info("[LIVE] Starting frostImportJob end-to-end execution test with REAL data");

        // Skip if Spring context is not available (for example frost.enabled=false)
        assumeTrue(jobLauncher != null && frostImportJob != null && frostProperties != null,
                "FROST components not autowired (likely disabled in test config)");

        // Log current configuration
        log.info("[LIVE] FROST Configuration:");
        log.info("[LIVE]   enabled: {}", frostProperties.isEnabled());
        log.info("[LIVE]   base-url: {}", frostProperties.getBaseUrl());
        log.info("[LIVE]   id-param: {}", frostProperties.getIdParam());
        log.info("[LIVE]   page-size: {}", frostProperties.getPageSize());
        log.info("[LIVE]   follow-pagination-links: {}", frostProperties.isFollowPaginationLinks());

        // Execute the real Spring Batch job with real data and real writer/DB
        long executionStart = System.nanoTime();
        log.info("[LIVE] Executing frostImportJob with real datastreams and observations...");

        JobParameters params = new JobParametersBuilder()
                .addLong("batch.id", 5L)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(frostImportJob, params);

        long executionTimeMs = (System.nanoTime() - executionStart) / 1_000_000;
        log.info("[LIVE] frostImportJob REAL execution completed in {} ms", executionTimeMs);
        log.info("[LIVE] Job status: {}", execution.getStatus());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

}