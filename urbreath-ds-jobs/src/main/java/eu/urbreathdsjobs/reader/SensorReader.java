package eu.urbreathdsjobs.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorReader {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Verifica se un sensore con il dato ID esterno esiste già nel database.
     *
     * @param externalSensorId identificativo esterno del sensore da cercare nel metadata
     * @return true se il sensore NON esiste (è possibile inserirlo), false se esiste già
     */
    public boolean isSensorNotExists(String externalSensorId) {
        if (externalSensorId == null || externalSensorId.trim().isEmpty()) {
            log.warn("externalSensorId è vuoto o null, considerato come non trovato");
            return true;
        }

        String sql = "SELECT COUNT(*) FROM public.sensor WHERE metadata->>'SENSOR_ID_EXTERNAL' = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, externalSensorId);

        boolean notExists = (count == null || count == 0);

        if (notExists) {
            log.debug("Sensore con SENSOR_ID_EXTERNAL={} non esiste, è possibile inserirlo", externalSensorId);
        } else {
            log.info("Sensore con SENSOR_ID_EXTERNAL={} esiste già, skippo l'inserimento", externalSensorId);
        }

        return notExists;
    }

    /**
     * Verifica se un sensore con il dato ID esterno esiste già nel database.
     * Metodo alternativo che ritorna il risultato inverso per logica più intuitiva.
     *
     * @param externalSensorId identificativo esterno del sensore da cercare nel metadata
     * @return true se il sensore esiste già, false se non esiste
     */
    public boolean sensorExists(String externalSensorId) {
        return !isSensorNotExists(externalSensorId);
    }

    /**
     * Ritorna il numero di sensori con il dato ID esterno.
     *
     * @param externalSensorId identificativo esterno del sensore
     * @return numero di sensori trovati (dovrebbe essere 0 o 1)
     */
    public int countSensorByExternalId(String externalSensorId) {
        if (externalSensorId == null || externalSensorId.trim().isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM public.sensor WHERE metadata->>'SENSOR_ID_EXTERNAL' = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, externalSensorId);
    }

}

