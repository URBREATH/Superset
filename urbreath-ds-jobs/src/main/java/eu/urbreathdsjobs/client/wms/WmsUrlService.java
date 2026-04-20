package eu.urbreathdsjobs.client.wms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating WMS request URLs for all cities and call types
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WmsUrlService {

    private final WmsProperties wmsProperties;

    /**
     * Generates all WMS URLs for a specific city
     *
     * @param city the city for which to generate URLs
     * @return list of WmsRequest objects with generated URLs
     */
    public List<WmsRequest> generateUrlsForCity(City city) {
        List<WmsRequest> requests = new ArrayList<>();

        WmsProperties.CityConfig cityConfig = wmsProperties.getCities().get(city.getName());
        if (cityConfig == null) {
            log.warn("City configuration not found for: {}", city.getName());
            return requests;
        }

        // Iterate through all call types
        for (String callTypeName : wmsProperties.getCallTypes().keySet()) {
            WmsProperties.CallTypeConfig callConfig = wmsProperties.getCallTypes().get(callTypeName);
            List<Integer> measures = callConfig.getMeasures();

            if (measures == null || measures.isEmpty()) {
                log.warn("No measures configured for callType: {}", callTypeName);
                continue;
            }

            // Generate a URL for each measure
            for (Integer measure : measures) {
                String url = buildUrl(
                        city,
                        callTypeName,
                        callConfig.getLayer(),
                        callConfig.getKey(),
                        cityConfig.getBbox(),
                        cityConfig.getTimezone(),
                        cityConfig.getI(),
                        cityConfig.getJ(),
                        measure
                );

                WmsRequest request = WmsRequest.builder()
                        .city(city)
                        .callType(callTypeName)
                        .measure(measure)
                        .url(url)
                        .layer(callConfig.getLayer())
                        .key(callConfig.getKey())
                        .bbox(cityConfig.getBbox())
                        .timezone(cityConfig.getTimezone())
                        .i(cityConfig.getI())
                        .j(cityConfig.getJ())
                        .build();

                requests.add(request);
                log.debug("Generated WMS URL for city: {}, callType: {}, measure: {}", city.getName(), callTypeName, measure);
            }
        }

        return requests;
    }

    /**
     * Generates all WMS URLs for all cities
     *
     * @return list of WmsRequest objects with generated URLs
     */
    public List<WmsRequest> generateUrlsForAllCities() {
        List<WmsRequest> allRequests = new ArrayList<>();

        for (City city : City.values()) {
            allRequests.addAll(generateUrlsForCity(city));
        }

        log.info("Generated {} total WMS URLs", allRequests.size());
        return allRequests;
    }

    /**
     * Generates a WMS URL with the specified parameters
     *
     * @param city the city
     * @param callType the call type (mintemperature, maxtemperature, precipitation, wind)
     * @param layer the layer name
     * @param key the threshold key
     * @param bbox the bounding box
     * @param timezone the timezone
     * @param measure the measure value
     * @return the formatted WMS URL
     */
    public String generateUrl(City city, String callType, String layer, String key, String bbox, String timezone, Integer i, Integer j, Integer measure) {
        return buildUrl(city, callType, layer, key, bbox, timezone, i, j, measure);
    }

    /**
     * Builds a WMS URL using the builder
     */
    private String buildUrl(City city, String callType, String layer, String key, String bbox, String timezone, Integer i, Integer j, Integer measure) {
        return WmsUrlBuilder.builder()
                .baseUrl(wmsProperties.getBaseUrl())
                .city(city)
                .callType(callType)
                .layer(layer)
                .key(key)
                .bbox(bbox)
                .timezone(timezone)
                .i(i)
                .j(j)
                .measure(measure)
                .build()
                .build();
    }
}

