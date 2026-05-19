package eu.urbreathdsjobs.writer;

import eu.urbreathdsjobs.model.TrafficMeasurement;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrafficCompositeWriter {

	private final TrafficSpeedHistogramWriter trafficSpeedHistogramWriter;

	@Qualifier("trafficMeasurementImportWriter")
	private final ItemWriter<TrafficMeasurement> trafficDataImportWriter;

	@Bean
	public CompositeItemWriter<TrafficMeasurement> compositeTrafficItemWriter() {
	    List<ItemWriter<? super TrafficMeasurement>> writers = new ArrayList<>();
	    writers.add(trafficDataImportWriter);  
	    writers.add(trafficSpeedHistogramWriter);  

	    CompositeItemWriter<TrafficMeasurement> compositeItemWriter = new CompositeItemWriter<>();
	    compositeItemWriter.setDelegates(writers);
	    return compositeItemWriter;
	}
	
  

}
