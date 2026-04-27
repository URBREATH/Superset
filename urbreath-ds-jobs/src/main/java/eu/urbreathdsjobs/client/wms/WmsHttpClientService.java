package eu.urbreathdsjobs.client.wms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.urbreathdsjobs.client.wms.dto.WmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WmsHttpClientService {

    private final ObjectMapper objectMapper;
    private final WmsUrlService wmsUrlService;
    private final HttpClient httpClient;

    @Autowired
    public WmsHttpClientService(ObjectMapper objectMapper, WmsUrlService wmsUrlService, HttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.wmsUrlService = wmsUrlService;
        this.httpClient = httpClient;
    }

    public List<WmsCallResult> fetchAllForCity(City city) {
        List<WmsRequest> requests = wmsUrlService.generateUrlsForCity(city);
        List<WmsCallResult> results = new ArrayList<>();

        for (WmsRequest request : requests) {
            try {
                results.add(fetch(request));
            } catch (RuntimeException ex) {
                log.warn("WMS call failed for city={}, callType={}. Cause: {}",
                        request.getCity(), request.getCallType(), ex.getMessage());
            }
        }

        return results;
    }

    public WmsCallResult fetch(WmsRequest request) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(request.getUrl()))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Unexpected HTTP status: " + response.statusCode());
            }

            WmsResponse mapped = parseResponse(response.body());
            return WmsCallResult.builder()
                    .request(request)
                    .statusCode(response.statusCode())
                    .response(mapped)
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("WMS HTTP call error", e);
        } catch (IOException e) {
            throw new RuntimeException("WMS HTTP call error", e);
        }
    }

    public WmsResponse parseResponse(String jsonBody) {
        try {
            return objectMapper.readValue(jsonBody, WmsResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to map WMS response", e);
        }
    }
}


