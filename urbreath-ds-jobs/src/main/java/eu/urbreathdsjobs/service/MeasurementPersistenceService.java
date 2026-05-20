package eu.urbreathdsjobs.service;

import eu.urbreathdsjobs.dao.MeasurementDao;
import eu.urbreathdsjobs.dao.SensorDao;
import eu.urbreathdsjobs.model.Measurement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MeasurementPersistenceService {

    private static final int CHUNK_SIZE = 500;

    private final MeasurementDao measurementDao;
    private final SensorDao sensorDao;

    public void deleteByIdParam(Long idParam) {
        measurementDao.deleteByIdParam(idParam);
    }

    public void deleteBySensorIds(List<Long> sensorIds) {
        if (sensorIds == null || sensorIds.isEmpty()) {
            log.warn("No sensor ids provided for deletion. Skipping delete by id_sensor.");
            return;
        }
        measurementDao.deleteBySensorIds(sensorIds);
    }

    public void writeMeasurements(List<? extends Measurement> measurements) {
        measurementDao.batchInsert(measurements);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeMeasurementsAndUpdateLastObservationDate(
            Long sensorId,
            String lastObservationDate,
            List<? extends Measurement> measurements
    ) {
        if (measurements == null || measurements.isEmpty()) {
            log.warn("No measurements to write for sensorId={}. Skipping write and metadata update.", sensorId);
            return;
        }

        measurementDao.batchInsert(measurements);
        sensorDao.updateLastObservationDate(sensorId, lastObservationDate);

        log.info("Persisted {} measurements and updated LAST_OBSERVATION_DATE for sensorId={}",
                measurements.size(), sensorId);
    }

    public void deleteAndWriteBySensorIds(List<Long> sensorIds, List<Measurement> measurements) {
        deleteBySensorIds(sensorIds);

        int total = measurements == null ? 0 : measurements.size();
        for (int i = 0; i < total; i += CHUNK_SIZE) {
            List<Measurement> chunk = measurements.subList(i, Math.min(i + CHUNK_SIZE, total));
            measurementDao.batchInsert(chunk);
            log.debug("Written chunk [{}-{}] of {}", i, i + chunk.size(), total);
        }

        log.info("deleteAndWriteBySensorIds completed for sensorsCount={}, inserted={}",
                sensorIds != null ? sensorIds.size() : 0,
                total);
    }
}

