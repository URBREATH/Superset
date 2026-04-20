package eu.urbreathdsjobs.client.wms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for WmsUrlService
 */
@DisplayName("WMS URL Service Tests")
class WmsUrlServiceTest {

    private WmsUrlService wmsUrlService;
    private WmsProperties wmsProperties;

    @BeforeEach
    void setUp() {
        // Initialize WmsProperties with test data
        wmsProperties = new WmsProperties();
        wmsProperties.setBaseUrl("https://urbreath.simena.red/wms/wms/");

        // Setup cities
        Map<String, WmsProperties.CityConfig> cities = new HashMap<>();
        WmsProperties.CityConfig madrid = new WmsProperties.CityConfig();
        madrid.setBbox("-413371.4489662349%2C4916429.659302536%2C-412148.4565136721%2C4917652.651755099");
        madrid.setTimezone("Europe%2FMadrid");
        madrid.setI(101);
        madrid.setJ(367);
        cities.put("MADRID", madrid);

        WmsProperties.CityConfig leuven = new WmsProperties.CityConfig();
        leuven.setBbox("4.403838%2C50.870514%2C4.404838%2C50.871514");
        leuven.setTimezone("Europe%2FBrussels");
        leuven.setI(101);
        leuven.setJ(367);
        cities.put("LEUVEN", leuven);

        wmsProperties.setCities(cities);

        // Setup call types
        Map<String, WmsProperties.CallTypeConfig> callTypes = new HashMap<>();

        WmsProperties.CallTypeConfig precipitation = new WmsProperties.CallTypeConfig();
        precipitation.setLayer("FIC_SAMPLE_PROB_PRECIPITATION_24H_R00R12_URB");
        precipitation.setKey("FIC_THRESHOLD_PRECIPITATION_24H_URB");
        precipitation.setMeasures(List.of(0, 10, 50));
        callTypes.put("precipitation", precipitation);

        WmsProperties.CallTypeConfig wind = new WmsProperties.CallTypeConfig();
        wind.setLayer("FIC_SAMPLE_PROB_GUSTMAX_R00R12_URB");
        wind.setKey("FIC_THRESHOLD_GUSTMAX_URB");
        wind.setMeasures(List.of(20, 30));
        callTypes.put("wind", wind);

        wmsProperties.setCallTypes(callTypes);

        // Create service with properties
        wmsUrlService = new WmsUrlService(wmsProperties);
    }

    @Test
    @DisplayName("Should generate URLs for a specific city")
    void testGenerateUrlsForCity() {
        List<WmsRequest> urls = wmsUrlService.generateUrlsForCity(City.MADRID);

        assertNotNull(urls);
        assertEquals(5, urls.size()); // precipitation (3) + wind (2)

        // Verify first URL
        WmsRequest firstUrl = urls.get(0);
        assertNotNull(firstUrl.getUrl());
        assertTrue(firstUrl.getUrl().contains("MADRID") || firstUrl.getUrl().contains("FIC_"));
        assertEquals(City.MADRID, firstUrl.getCity());
    }

    @Test
    @DisplayName("Should generate correct URL format")
    void testUrlFormat() {
        String url = wmsUrlService.generateUrl(
                City.MADRID,
                "wind",
                "FIC_SAMPLE_PROB_GUSTMAX_R00R12_URB",
                "FIC_THRESHOLD_GUSTMAX_URB",
                "-413371.4489662349%2C4916429.659302536%2C-412148.4565136721%2C4917652.651755099",
                "Europe%2FMadrid",
                101,
                367,
                50
        );

        assertNotNull(url);
        assertTrue(url.contains("REQUEST=GetFeatureInfo"));
        assertTrue(url.contains("SERVICE=WMS"));
        assertTrue(url.contains("VERSION=1.3.0"));
        assertTrue(url.contains("FIC_SAMPLE_PROB_GUSTMAX_R00R12_URB"));
        assertTrue(url.contains("FIC_THRESHOLD_GUSTMAX_URB=50"));
        assertTrue(url.contains("BBOX="));
        assertTrue(url.contains("CRS=EPSG%3A3857"));
    }

    @Test
    @DisplayName("Should contain all required WMS parameters")
    void testUrlContainsAllParameters() {
        String url = wmsUrlService.generateUrl(
                City.MADRID,
                "precipitation",
                "FIC_SAMPLE_PROB_PRECIPITATION_24H_R00R12_URB",
                "FIC_THRESHOLD_PRECIPITATION_24H_URB",
                "-413371.4489662349%2C4916429.659302536%2C-412148.4565136721%2C4917652.651755099",
                "Europe%2FMadrid",
                101,
                367,
                20
        );

        assertTrue(url.contains("REQUEST=GetFeatureInfo"), "Missing REQUEST parameter");
        assertTrue(url.contains("SERVICE=WMS"), "Missing SERVICE parameter");
        assertTrue(url.contains("FORMAT=image%2Fpng"), "Missing FORMAT parameter");
        assertTrue(url.contains("TRANSPARENT=TRUE"), "Missing TRANSPARENT parameter");
        assertTrue(url.contains("HORIZON=__all__"), "Missing HORIZON parameter");
        assertTrue(url.contains("INFO_FORMAT=application%2Fjson"), "Missing INFO_FORMAT parameter");
        assertTrue(url.contains("I=101"), "Missing I parameter");
        assertTrue(url.contains("J=367"), "Missing J parameter");
        assertTrue(url.contains("WIDTH=512"), "Missing WIDTH parameter");
        assertTrue(url.contains("HEIGHT=512"), "Missing HEIGHT parameter");
        assertTrue(url.contains("CRS=EPSG%3A3857"), "Missing CRS parameter");
        assertTrue(url.contains("FIC_THRESHOLD_PRECIPITATION_24H_URB=20"), "Missing dynamic threshold parameter");
        assertTrue(url.contains("I=101"), "Missing I parameter");
        assertTrue(url.contains("J=367"), "Missing J parameter");
    }

    @Test
    @DisplayName("Should use correct measure values in URLs")
    void testMeasureValuesInUrl() {
        List<WmsRequest> urls = wmsUrlService.generateUrlsForCity(City.MADRID);

        // Check that all expected measure values are present
        List<Integer> foundMeasures = urls.stream()
                .map(WmsRequest::getMeasure)
                .distinct()
                .toList();

        assertEquals(5, foundMeasures.size());
        assertTrue(foundMeasures.contains(0));
        assertTrue(foundMeasures.contains(10));
        assertTrue(foundMeasures.contains(20));
        assertTrue(foundMeasures.contains(30));
        assertTrue(foundMeasures.contains(50));
    }

    @Test
    @DisplayName("Should return empty list for non-existent city")
    void testNonExistentCity() {
        List<WmsRequest> urls = wmsUrlService.generateUrlsForCity(City.CLUJ);
        assertTrue(urls.isEmpty());
    }

    @Test
    @DisplayName("WmsRequest should contain all details")
    void testWmsRequestDetails() {
        List<WmsRequest> urls = wmsUrlService.generateUrlsForCity(City.MADRID);
        WmsRequest request = urls.get(0);

        assertNotNull(request.getCity());
        assertNotNull(request.getCallType());
        assertNotNull(request.getMeasure());
        assertNotNull(request.getUrl());
        assertNotNull(request.getLayer());
        assertNotNull(request.getKey());
        assertNotNull(request.getBbox());
        assertNotNull(request.getTimezone());
        assertNotNull(request.getI());
        assertNotNull(request.getJ());
    }
}

