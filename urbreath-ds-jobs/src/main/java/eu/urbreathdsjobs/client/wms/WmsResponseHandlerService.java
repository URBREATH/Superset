package eu.urbreathdsjobs.client.wms;

import eu.urbreathdsjobs.client.wms.dto.WmsFeature;
import eu.urbreathdsjobs.client.wms.dto.WmsLayerCollection;
import eu.urbreathdsjobs.client.wms.dto.WmsResponse;
import eu.urbreathdsjobs.client.wms.sensor.WmsSensorRegistry;
import eu.urbreathdsjobs.common.Constants;
import eu.urbreathdsjobs.common.MeasurementAttributeEnum;
import eu.urbreathdsjobs.model.Measurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WmsResponseHandlerService {

    private final WmsSensorRegistry wmsSensorRegistry;
    private final WmsProperties wmsProperties;

    public List<Measurement> handle(WmsCallResult result) {
        Long configuredIdParam = wmsProperties.getIdParam();

        if (result == null || result.getResponse() == null) {
            log.warn("WMS response is null, skipping processing");
            return new ArrayList<>();
        }

        WmsResponse response = result.getResponse();
        WmsRequest request = result.getRequest();
        City city = request != null ? request.getCity() : null;
        String key = request != null ? request.getKey() : null;
        String sensor = wmsSensorRegistry.getSensor(city, key);
        if ( sensor==null || sensor.isEmpty() || configuredIdParam==null ){
            throw new RuntimeException(String.format("Missing configuration for city=%s, key=%s, sensor=%s, idParam=%s", city, key, sensor, configuredIdParam));
        }

        log.info(
                "Starting Processing WMS response for city={}, callType={}, key={}, sensor={}",
                city,
                request != null ? request.getCallType() : null,
                key,
                sensor
        );

        List<WmsLayerCollection> layers = response.getFeatures();
        int layerCount = layers == null ? 0 : layers.size();
        List<Measurement> listMeasurament = new ArrayList<>();
        if (layers != null) {
            for (WmsLayerCollection layer : layers) {
                if (layer.getFeatures() == null) {
                    log.warn("layer.getFeatures() == null, skipping processing");
                    continue;
                }

                for (WmsFeature feature : layer.getFeatures()) {
                    if (feature == null || feature.getProperties() == null) {
                        log.warn("feature.getProperties() == null, skipping processing");
                        continue;
                    }
                    String codiceScenario = feature.getName();

                    Map<String, com.fasterxml.jackson.databind.JsonNode> dimensionValues =
                            feature.getProperties().getDimensionValue();

                    // Estrai HOR (orizzonte temporale) - es: 0
                    Double horValue = null;
                    if (dimensionValues != null && dimensionValues.containsKey(MeasurementAttributeEnum.HOR.name())) {
                        horValue = dimensionValues.get(MeasurementAttributeEnum.HOR.name()).asDouble();
                    }

                    // Estrai il valore soglia usando la chiave WMS (es: FIC_THRESHOLD_GUSTMAX_URB) - es: 60
                    Double thresholdValue = null;
                    if (dimensionValues != null && key != null && dimensionValues.containsKey(key)) {
                        thresholdValue = dimensionValues.get(key).asDouble();
                    }

                    Measurement measurement = new Measurement();
                    measurement.setIdParam(configuredIdParam);
                    measurement.setIdSensor(Long.valueOf(sensor));
                    measurement.setVal(feature.getProperties().getValue());
                    String baseTime = feature.getProperties().getBaseTime();
                    LocalDateTime observationDate = baseTime != null
                            ? ZonedDateTime.parse(baseTime).toLocalDateTime()
                            : null;
                    measurement.setDateFrom(observationDate);
                    measurement.setDateTo(observationDate);
                    measurement.setPeriod("1day");
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put(MeasurementAttributeEnum.MEASURE_TYPE.name(), Constants.MEASUREMENT_TYPE_PROJECTION);
                    metadata.put(MeasurementAttributeEnum.MEASURE_SIMULATION_COD_SCENARIO.name(), codiceScenario);
                    metadata.put(MeasurementAttributeEnum.HOR.name(), horValue);
                    metadata.put(key, thresholdValue);
                    measurement.setMetadata(metadata);
                    listMeasurament.add(measurement);
                    log.debug(
                            "Feature: name={}, type={}, baseTime={}, value={}, HOR={}, thresholdKey={}, thresholdValue={}",
                            feature.getName(),
                            feature.getType(),
                            feature.getProperties().getBaseTime(),
                            feature.getProperties().getValue(),
                            horValue,
                            key,
                            thresholdValue
                    );
                }
            }
        }



        log.info(
                "Processed WMS response for city={}, callType={}, key={}, sensor={}, layers={}",
                city,
                request != null ? request.getCallType() : null,
                key,
                sensor,
                layerCount);

        return listMeasurament;
    }
}
