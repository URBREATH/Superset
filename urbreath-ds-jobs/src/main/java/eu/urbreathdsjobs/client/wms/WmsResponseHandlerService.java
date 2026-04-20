package eu.urbreathdsjobs.client.wms;

import eu.urbreathdsjobs.client.wms.dto.WmsLayerCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WmsResponseHandlerService {

    public void handle(WmsCallResult result) {
        if (result == null || result.getResponse() == null) {
            log.warn("WMS response is null, skipping processing");
            return;
        }

        List<WmsLayerCollection> layers = result.getResponse().getFeatures();
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
                "Processed WMS response for city={}, callType={}, measure={}, layers={}, features={}",
                result.getRequest() != null ? result.getRequest().getCity() : null,
                result.getRequest() != null ? result.getRequest().getCallType() : null,
                result.getRequest() != null ? result.getRequest().getMeasure() : null,
                layerCount,
                featureCount
        );
    }
}

