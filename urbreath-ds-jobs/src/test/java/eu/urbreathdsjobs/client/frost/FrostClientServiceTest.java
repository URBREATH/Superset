package eu.urbreathdsjobs.client.frost;

import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.dao.DatastreamDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FROST client service tests")
class FrostClientServiceTest {

    @Test
    @DisplayName("Should populate datastream config filter from global frost filter")
    void shouldPopulateDatastreamConfigFilterFromGlobalFilter() {
        SensorThingsService sensorThingsService = mock(SensorThingsService.class);
        FrostProperties frostProperties = new FrostProperties();
        frostProperties.setFilter("result ne null");

        FrostProperties.DatastreamConfig datastreamConfig = new FrostProperties.DatastreamConfig();
        datastreamConfig.setDatastreamId(1L);
        datastreamConfig.setSensorId(1001L);
        datastreamConfig.setEnabled(true);
        datastreamConfig.setFilter(null);
        frostProperties.setDatastreams(List.of(datastreamConfig));

        FrostClientService frostClientService = new FrostClientService(sensorThingsService, frostProperties);
        List<FrostProperties.DatastreamConfig> configured = frostClientService.getConfiguredDatastreams();

        assertEquals(1, configured.size());
        assertEquals("result ne null", configured.get(0).getFilter());
    }


    @Test
    @DisplayName("Should discover datastream configs from server when frost.datastreams is empty")
    void shouldDiscoverDatastreamConfigsWhenConfigIsEmpty() throws Exception {
        SensorThingsService sensorThingsService = mock(SensorThingsService.class);
        DatastreamDao datastreamDao = mock(DatastreamDao.class);
        Query query = mock(Query.class);
        @SuppressWarnings("unchecked")
        EntityList<Datastream> entityList = mock(EntityList.class);
        @SuppressWarnings("unchecked")
        EntityList<Datastream> emptyEntityList = mock(EntityList.class);

        Datastream datastream = mock(Datastream.class);
        Sensor sensor = mock(Sensor.class);
        ObservedProperty observedProperty = mock(ObservedProperty.class);
        Id datastreamId = mock(Id.class);
        Id sensorId = mock(Id.class);

        when(datastreamId.getValue()).thenReturn(99L);
        when(sensorId.getValue()).thenReturn(1001L);
        when(datastream.getId()).thenReturn(datastreamId);
        when(datastream.getSensor()).thenReturn(sensor);
        when(sensor.getId()).thenReturn(sensorId);
        when(datastream.getObservedProperty()).thenReturn(observedProperty);
        when(observedProperty.getName()).thenReturn("temperature");
        when(datastream.getDescription()).thenReturn("Test stream");

        FrostProperties frostProperties = new FrostProperties();
        frostProperties.setDatastreams(List.of());
        frostProperties.setFollowPaginationLinks(true);
        frostProperties.setPageSize(1);

        when(sensorThingsService.datastreams()).thenReturn(datastreamDao);
        when(datastreamDao.query()).thenReturn(query);
        when(query.top(anyInt())).thenReturn(query);
        when(query.skip(anyInt())).thenReturn(query);
        when(query.list()).thenReturn(entityList).thenReturn(emptyEntityList);
        when(entityList.iterator()).thenReturn(List.of(datastream).iterator());
        when(emptyEntityList.iterator()).thenReturn(List.<Datastream>of().iterator());

        FrostClientService frostClientService = new FrostClientService(sensorThingsService, frostProperties);
        List<FrostProperties.DatastreamConfig> configs = frostClientService.getConfiguredDatastreams();

        assertEquals(1, configs.size());
        assertEquals(99L, configs.get(0).getDatastreamId());
        assertEquals(1001L, configs.get(0).getSensorId());
        assertEquals("temperature", configs.get(0).getObservedProperty());
        assertTrue(configs.get(0).isEnabled());
    }

    @Test
    @DisplayName("Should build SensorThings query with filter, order, page size and expand")
    void shouldBuildSensorThingsQueryWithConfiguredOptions() throws Exception {
        SensorThingsService sensorThingsService = mock(SensorThingsService.class);
        ObservationDao observationDao = mock(ObservationDao.class);
        Query query = mock(Query.class);
        @SuppressWarnings("unchecked")
        EntityList<Observation> entityList = mock(EntityList.class);

        FrostProperties frostProperties = new FrostProperties();
        frostProperties.setPageSize(250);
        frostProperties.setOrderBy("phenomenonTime desc");
        frostProperties.setExpand("Datastream($expand=Thing,Sensor,ObservedProperty)");

        FrostProperties.DatastreamConfig datastreamConfig = new FrostProperties.DatastreamConfig();
        datastreamConfig.setDatastreamId(99L);
        datastreamConfig.setSensorId(1001L);
        datastreamConfig.setCity("MADRID");
        datastreamConfig.setObservedProperty("temperature");
        datastreamConfig.setFilter("result ne null");

        Observation observation = new Observation();
        Iterator<Observation> iterator = List.of(observation).iterator();

        when(sensorThingsService.observations()).thenReturn(observationDao);
        when(observationDao.query()).thenReturn(query);
        when(query.filter(anyString())).thenReturn(query);
        when(query.orderBy(anyString())).thenReturn(query);
        when(query.top(anyInt())).thenReturn(query);
        when(query.expand(anyString())).thenReturn(query);
        when(query.list()).thenReturn(entityList);
        when(entityList.fullIterator()).thenReturn(iterator);

        FrostClientService frostClientService = new FrostClientService(sensorThingsService, frostProperties);
        List<Observation> observations = frostClientService.fetchObservations(datastreamConfig);

        assertEquals(1, observations.size());
        assertSame(observation, observations.get(0));

        ArgumentCaptor<String> filterCaptor = ArgumentCaptor.forClass(String.class);
        verify(query).filter(filterCaptor.capture());
        assertEquals("(Datastream/id eq 99) and (result ne null)", filterCaptor.getValue());
        verify(query).orderBy("phenomenonTime desc");
        verify(query).top(250);
        verify(query).expand("Datastream($expand=Thing,Sensor,ObservedProperty)");
    }

    @Test
    @DisplayName("Should use global frost filter when datastream filter is missing")
    void shouldUseGlobalFrostFilterWhenDatastreamFilterIsMissing() throws Exception {
        SensorThingsService sensorThingsService = mock(SensorThingsService.class);
        ObservationDao observationDao = mock(ObservationDao.class);
        Query query = mock(Query.class);
        @SuppressWarnings("unchecked")
        EntityList<Observation> entityList = mock(EntityList.class);

        FrostProperties frostProperties = new FrostProperties();
        frostProperties.setPageSize(50);
        frostProperties.setFilter("result ne null");

        FrostProperties.DatastreamConfig datastreamConfig = new FrostProperties.DatastreamConfig();
        datastreamConfig.setDatastreamId(77L);
        datastreamConfig.setSensorId(88L);

        Observation observation = new Observation();

        when(sensorThingsService.observations()).thenReturn(observationDao);
        when(observationDao.query()).thenReturn(query);
        when(query.filter(anyString())).thenReturn(query);
        when(query.top(anyInt())).thenReturn(query);
        when(query.list()).thenReturn(entityList);
        when(entityList.fullIterator()).thenReturn(List.of(observation).iterator());

        FrostClientService frostClientService = new FrostClientService(sensorThingsService, frostProperties);
        List<Observation> observations = frostClientService.fetchObservations(datastreamConfig);

        assertEquals(1, observations.size());
        assertSame(observation, observations.get(0));

        ArgumentCaptor<String> filterCaptor = ArgumentCaptor.forClass(String.class);
        verify(query).filter(filterCaptor.capture());
        assertEquals("(Datastream/id eq 77) and (result ne null)", filterCaptor.getValue());
    }

    @Test
    @DisplayName("Should read only first page when followPaginationLinks is disabled")
    void shouldReadOnlyFirstPageWhenFollowPaginationLinksDisabled() throws Exception {
        SensorThingsService sensorThingsService = mock(SensorThingsService.class);
        ObservationDao observationDao = mock(ObservationDao.class);
        Query query = mock(Query.class);
        @SuppressWarnings("unchecked")
        EntityList<Observation> entityList = mock(EntityList.class);

        FrostProperties frostProperties = new FrostProperties();
        frostProperties.setPageSize(2);
        frostProperties.setFollowPaginationLinks(false);

        FrostProperties.DatastreamConfig datastreamConfig = new FrostProperties.DatastreamConfig();
        datastreamConfig.setDatastreamId(7L);
        datastreamConfig.setSensorId(8L);

        Observation first = new Observation();
        Observation second = new Observation();

        when(sensorThingsService.observations()).thenReturn(observationDao);
        when(observationDao.query()).thenReturn(query);
        when(query.filter(anyString())).thenReturn(query);
        when(query.top(anyInt())).thenReturn(query);
        when(query.list()).thenReturn(entityList);
        when(entityList.iterator()).thenReturn(List.of(first, second).iterator());

        FrostClientService frostClientService = new FrostClientService(sensorThingsService, frostProperties);
        List<Observation> observations = frostClientService.fetchObservations(datastreamConfig);

        assertEquals(2, observations.size());
        assertSame(first, observations.get(0));
        assertSame(second, observations.get(1));

        verify(entityList).iterator();
        verify(entityList, never()).fullIterator();
    }

    @Test
    @DisplayName("Should fetch a specific page using top and skip")
    void shouldFetchSpecificPageUsingTopAndSkip() throws Exception {
        SensorThingsService sensorThingsService = mock(SensorThingsService.class);
        ObservationDao observationDao = mock(ObservationDao.class);
        Query query = mock(Query.class);
        @SuppressWarnings("unchecked")
        EntityList<Observation> entityList = mock(EntityList.class);

        FrostProperties frostProperties = new FrostProperties();
        frostProperties.setPageSize(300);

        FrostProperties.DatastreamConfig datastreamConfig = new FrostProperties.DatastreamConfig();
        datastreamConfig.setDatastreamId(11L);
        datastreamConfig.setSensorId(12L);

        Observation observation = new Observation();

        when(sensorThingsService.observations()).thenReturn(observationDao);
        when(observationDao.query()).thenReturn(query);
        when(query.filter(anyString())).thenReturn(query);
        when(query.orderBy(anyString())).thenReturn(query);
        when(query.expand(anyString())).thenReturn(query);
        when(query.top(anyInt())).thenReturn(query);
        when(query.skip(anyInt())).thenReturn(query);
        when(query.list()).thenReturn(entityList);
        when(entityList.iterator()).thenReturn(List.of(observation).iterator());

        FrostClientService frostClientService = new FrostClientService(sensorThingsService, frostProperties);
        List<Observation> observations = frostClientService.fetchObservationsPage(datastreamConfig, 2);

        assertEquals(1, observations.size());
        assertSame(observation, observations.get(0));
        verify(query).top(300);
        verify(query).skip(600);
    }
}


