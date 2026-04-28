package eu.urbreathdsjobs.client.frost;

import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FROST client service tests")
class FrostClientServiceTest {

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


