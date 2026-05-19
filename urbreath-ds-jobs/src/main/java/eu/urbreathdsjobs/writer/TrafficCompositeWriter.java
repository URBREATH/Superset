package eu.urbreathdsjobs.writer;

import eu.urbreathdsjobs.model.TrafficMeasurement;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrafficCompositeWriter {
	
	
	
	private final TrafficSpeedHistogramWriter trafficSpeedHistogramWriter;
	
	private final JdbcBatchItemWriter<TrafficMeasurement> trafficDataImportWriter;
	
	@Bean
	public CompositeItemWriter<TrafficMeasurement> compositeTrafficItemWriter(DataSource dataSource) {
	    List<ItemWriter<? super TrafficMeasurement>> writers = new ArrayList<>();
	    writers.add(trafficDataImportWriter);  
	    writers.add(trafficSpeedHistogramWriter);  

	    CompositeItemWriter<TrafficMeasurement> compositeItemWriter = new CompositeItemWriter<>();
	    compositeItemWriter.setDelegates(writers);
	    return compositeItemWriter;
	}
	
  

}
