package eu.urbreathdsjobs.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import eu.urbreathdsjobs.model.TrafficHistogram;
import eu.urbreathdsjobs.model.TrafficMeasurement;
import lombok.Data;
import lombok.RequiredArgsConstructor;

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
