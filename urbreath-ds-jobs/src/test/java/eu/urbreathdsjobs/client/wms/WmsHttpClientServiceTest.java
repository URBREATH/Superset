package eu.urbreathdsjobs.client.wms;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.urbreathdsjobs.client.wms.dto.WmsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WMS HTTP client mapping tests")
class WmsHttpClientServiceTest {

    @Test
    @DisplayName("Should map nested WMS JSON response")
    void shouldMapNestedWmsResponse() {
        String json = """
                {
                  "type": "FeatureCollection",
                  "features": [
                    {
                      "type": "FeatureCollection",
                      "features": [
                        {
                          "type": "Feature",
                          "name": "FIC_SAMPLE_PROB_PRECIPITATION_24H_R00R12_URB",
                          "geometry": {
                            "type": "Point",
                            "coordinates": [-3.711178, 40.348902]
                          },
                          "properties": {
                            "element": {
                              "id": 5296956,
                              "name": "J.M.D. Villaverde",
                              "locator": "28079103",
                              "element_type": {
                                "name": "Observatorios Comunidad de Madrid",
                                "pk": 1232,
                                "locatable": true,
                                "data": []
                              },
                              "type": {
                                "klass": "table",
                                "name": "Element"
                              },
                              "centroid": [-3.711178, 40.348902],
                              "geometry": "POINT (-3.711178 40.348902)",
                              "model": "saver.element",
                              "pk": 5296956,
                              "data": [],
                              "related": []
                            },
                            "id": 319875642,
                            "base_time": "2026-04-20T00:00:00Z",
                            "value": 0.0,
                            "dimension_value": {
                              "HOR": 0,
                              "FIC_THRESHOLD_PRECIPITATION_24H_URB": 10
                            },
                            "system_date": "2026-04-20T08:34:16.969Z",
                            "minutes": 541.2694531166667,
                            "time": "2026-04-20T00:00:00Z"
                          }
                        }
                      ],
                      "layer_name": "FIC_SAMPLE_PROB_PRECIPITATION_24H_R00R12_URB",
                      "layer_title": "FIC_SAMPLE_PROB_PRECIPITATION_24H_R00R12_URB",
                      "has_time": true
                    }
                  ]
                }
                """;

        WmsHttpClientService service = new WmsHttpClientService(new ObjectMapper(), null, HttpClient.newHttpClient());
        WmsResponse response = service.parseResponse(json);

        assertNotNull(response);
        assertEquals("FeatureCollection", response.getType());
        assertEquals(1, response.getFeatures().size());

        var layerCollection = response.getFeatures().get(0);
        assertEquals("FIC_SAMPLE_PROB_PRECIPITATION_24H_R00R12_URB", layerCollection.getLayerName());
        assertTrue(layerCollection.getHasTime());
        assertEquals(1, layerCollection.getFeatures().size());

        var feature = layerCollection.getFeatures().get(0);
        assertEquals("Feature", feature.getType());
        assertEquals("FIC_SAMPLE_PROB_PRECIPITATION_24H_R00R12_URB", feature.getName());
        assertEquals("Point", feature.getGeometry().getType());
        assertEquals(2, feature.getGeometry().getCoordinates().size());

        var props = feature.getProperties();
        assertEquals(319875642L, props.getId());
        assertEquals("2026-04-20T00:00:00Z", props.getBaseTime());
        assertNotNull(props.getDimensionValue());
        assertEquals(0, props.getDimensionValue().get("HOR").asInt());
        assertEquals(10, props.getDimensionValue().get("FIC_THRESHOLD_PRECIPITATION_24H_URB").asInt());

        assertNotNull(props.getElement());
        assertEquals(5296956L, props.getElement().getId());
        assertEquals("J.M.D. Villaverde", props.getElement().getName());
        assertEquals("table", props.getElement().getType().getKlass());
    }
}

