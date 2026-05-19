package eu.urbreathdsjobs.writer;

import eu.urbreathdsjobs.model.TrafficMeasurement;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class TrafficCompositeWriter {

	private final TrafficSpeedHistogramWriter trafficSpeedHistogramWriter;
	private final ItemWriter<TrafficMeasurement> trafficDataImportWriter;

	@Autowired
	public TrafficCompositeWriter(
			TrafficSpeedHistogramWriter trafficSpeedHistogramWriter,
			@Qualifier("trafficMeasurementImportWriter") ItemWriter<TrafficMeasurement> trafficDataImportWriter) {
		this.trafficSpeedHistogramWriter = trafficSpeedHistogramWriter;
		this.trafficDataImportWriter = trafficDataImportWriter;
	}

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
