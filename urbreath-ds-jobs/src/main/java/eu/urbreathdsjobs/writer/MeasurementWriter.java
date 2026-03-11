package eu.urbreathdsjobs.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.urbreathdsjobs.model.Measurement;
import lombok.Data;

@Component
@Data
@Transactional
public class MeasurementWriter implements ItemWriter<Measurement> {
	
	private final JdbcTemplate jdbcTemplate;
	
    private String insertMeasurement = """
INSERT INTO public.measurement
( id_param, id_sensor, "period", date_from, date_to, min, q02, q24, median, q75, q98, max, avg, sd, val)
VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
     private String insertAttribute = """
            INSERT INTO measurement_attribute (id_attribute, id_measure, attr_value_string, attr_value_number)
            VALUES (?, ?, ?, ?)
        """;

	

	@Override
	public void write(Chunk<? extends Measurement> chunk) throws Exception {
		chunk.getItems().stream().forEach(
			m -> {
	            Long idMeasure = jdbcTemplate.queryForObject(
		                insertMeasurement + " RETURNING id_measure",
		                Long.class,
		                m.getIdParam(), m.getIdSensor(), m.getPeriod(),
		                m.getDateFrom(), m.getDateTo(),
		                m.getMin(), m.getQ02(), m.getQ24(), m.getMedian(), m.getQ75(), m.getQ98(), m.getMax(), m.getAvg(), m.getSd()
		                , m.getVal()
		            );

		            // insert attributi in batch
		            jdbcTemplate.batchUpdate(insertAttribute,
		                m.getAttributes().stream()
		                    .map(attr -> new Object[]{
		                        attr.getIdAttribute(),
		                        idMeasure,
		                        attr.getAttrValueString(),
		                        attr.getAttrValueNumber()
		                    })
		                    .toList());
		            });
			
		

		
	}

}
