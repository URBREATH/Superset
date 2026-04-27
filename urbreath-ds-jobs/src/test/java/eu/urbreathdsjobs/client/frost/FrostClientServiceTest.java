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
}


