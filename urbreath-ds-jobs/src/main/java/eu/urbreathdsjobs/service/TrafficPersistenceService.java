package eu.urbreathdsjobs.service;

import eu.urbreathdsjobs.dao.TrafficHistogramDao;
import eu.urbreathdsjobs.dao.TrafficMeasurementDao;
import eu.urbreathdsjobs.model.TrafficHistogram;
import eu.urbreathdsjobs.model.TrafficMeasurement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class TrafficPersistenceService {

    private final TrafficMeasurementDao trafficMeasurementDao;
    private final TrafficHistogramDao trafficHistogramDao;

    public void writeMeasurements(List<? extends TrafficMeasurement> items) {
        trafficMeasurementDao.batchInsert(items);
    }

    public void writeHistograms(List<? extends TrafficHistogram> items) {
        trafficHistogramDao.batchInsert(items);
    }
}

