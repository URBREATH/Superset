package eu.urbreathdsjobs.writer;

import eu.urbreathdsjobs.model.TrafficHistogram;
import eu.urbreathdsjobs.model.TrafficMeasurement;
import eu.urbreathdsjobs.service.TrafficPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TrafficSpeedHistogramWriter implements ItemWriter<TrafficMeasurement> {	

	private final TrafficPersistenceService trafficPersistenceService;


	@Override
	public void write(Chunk<? extends TrafficMeasurement> chunk) {
		List<TrafficHistogram> trafficResList = new ArrayList<>();

		List<TrafficHistogram> trafficHistogram120List = chunk.getItems().stream()
				.map(TrafficMeasurement::getCarSpeedHist0to120plus)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.collect(Collectors.toList());
		List<TrafficHistogram> trafficHistogram70List = chunk.getItems().stream()
				.map(TrafficMeasurement::getCarSpeedHist0to70plus)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.collect(Collectors.toList());

		trafficResList.addAll(trafficHistogram70List);
		trafficResList.addAll(trafficHistogram120List);

		trafficPersistenceService.writeHistograms(trafficResList);
	}
	

	

}
