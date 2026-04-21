package eu.urbreathdsjobs.client.wms;

import eu.urbreathdsjobs.client.wms.dto.WmsLayerCollection;
import eu.urbreathdsjobs.client.wms.dto.WmsResponse;
import eu.urbreathdsjobs.client.wms.sensor.WmsSensorRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WmsResponseHandlerService {

    private final WmsSensorRegistry wmsSensorRegistry;

    public void handle(WmsCallResult result) {
        if (result == null || result.getResponse() == null) {
            log.warn("WMS response is null, skipping processing");
            return;
        }

        WmsResponse response = result.getResponse();
        WmsRequest request = result.getRequest();
        City city = request != null ? request.getCity() : null;
        String key = request != null ? request.getKey() : null;
        String sensor = wmsSensorRegistry.getSensor(city, key);

        if (city != null && key != null && sensor == null) {
            log.warn("No sensor mapping found for city={}, key={}", city, key);
        }

        List<WmsLayerCollection> layers = response.getFeatures();
        int layerCount = layers == null ? 0 : layers.size();
        int featureCount = 0;

        if (layers != null) {
            for (WmsLayerCollection layer : layers) {

                if (layer.getFeatures() != null) {
                    featureCount += layer.getFeatures().size();
                }
            }
        }


        // Placeholder: here you can persist data, publish events, or trigger downstream processing.
        log.info(
                "Processed WMS response for city={}, callType={}, measure={}, key={}, sensor={}, layers={}, features={}",
                city,
                request != null ? request.getCallType() : null,
                request != null ? request.getMeasure() : null,
                key,
                sensor,
                layerCount,
                featureCount
        );
    }
}

