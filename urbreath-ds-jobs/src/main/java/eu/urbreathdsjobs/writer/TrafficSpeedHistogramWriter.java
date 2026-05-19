package eu.urbreathdsjobs.writer;

import eu.urbreathdsjobs.model.TrafficHistogram;
import eu.urbreathdsjobs.model.TrafficMeasurement;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TrafficSpeedHistogramWriter implements ItemWriter<TrafficMeasurement> {	
	
	@Autowired
	private JdbcBatchItemWriter<TrafficHistogram>  trafficHistogramImportWriter;
	private final DataSource dataSource;


	@Override
	public void write(Chunk<? extends TrafficMeasurement> chunk) throws Exception {
		List<TrafficHistogram> trafficResList = new ArrayList<TrafficHistogram>();

		List<TrafficHistogram> trafficHistogram120List = chunk.getItems().stream().map(x -> x.getCarSpeedHist0to120plus()).flatMap(List::stream).collect(Collectors.toList());
		List<TrafficHistogram> trafficHistogram70List = chunk.getItems().stream().map(x -> x.getCarSpeedHist0to70plus()).flatMap(List::stream).collect(Collectors.toList());

		trafficResList.addAll(trafficHistogram70List);
		trafficResList.addAll(trafficHistogram120List);
		
		trafficHistogramImportWriter.write(new Chunk<TrafficHistogram>(trafficResList));
		
	}
	

	

}
