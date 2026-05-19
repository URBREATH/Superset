package eu.urbreathdsjobs.processor;

import eu.urbreathdsjobs.common.Constants;
import eu.urbreathdsjobs.common.MeasurementAttributeEnum;
import eu.urbreathdsjobs.dto.TaskJson;
import eu.urbreathdsjobs.dto.TaskJsonItem;
import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.model.TemperatureCsvRow;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class TemperatureProcessor  implements ItemProcessor<TemperatureCsvRow, Measurement>, StepExecutionListener{
	
	private StepExecution stepExecution;
	


	

	@Override
	public Measurement process(TemperatureCsvRow item) throws Exception {
		TaskJson taskJson = (TaskJson) this.stepExecution
		.getJobExecution()
		.getExecutionContext()
		.get("TASK_JSON");
		
        Long taskId = this.stepExecution.getJobExecution()
                .getExecutionContext()
                .getLong("TASK_ID");
		
		TaskJsonItem measurementTypeItem = taskJson.getItems().stream().filter(x -> x.getKey().equals(MeasurementAttributeEnum.MEASURE_TYPE.name())).findFirst().get();
		TaskJsonItem paramIdItem = taskJson.getItems().stream().filter(x -> x.getKey().equals(Constants.PARAM_ID)).findFirst().get();
		TaskJsonItem sensorIdItem = taskJson.getItems().stream().filter(x -> x.getKey().equals(Constants.SENSOR_ID)).findFirst().get();
		TaskJsonItem periodItem = taskJson.getItems().stream().filter(x -> x.getKey().equals(Constants.PERIOD)).findFirst().get();
        TaskJsonItem objectKeyItem = taskJson.getItems().stream().filter(x -> x.getKey().equals("objectKey")).findFirst().get();

		
		LocalDateTime observationDate = LocalDateTime.of(item.getYear(), item.getMonth(), item.getDay(), 0, 0);
		
		
		Measurement measurement = new Measurement();
		measurement.setIdParam(Long.valueOf(paramIdItem.getValue()) );
		measurement.setIdSensor(Long.valueOf(sensorIdItem.getValue()));
		measurement.setPeriod(periodItem.getValue());
		measurement.setDateFrom(observationDate);	
		measurement.setDateTo(observationDate);
		measurement.setMax(item.getTmax());
		measurement.setMin(item.getTmin());
		
		Double avgTermicTemp = null;
		
		if (item.getTmax() != null && item.getTmin() != null) {
			measurement.setAvg((item.getTmax() + item.getTmin()) / 2.0);
			avgTermicTemp = (double) (item.getTmin() + ((item.getTmax() - item.getTmin()) / Math.PI));
		}
		
		Map<String, Object> metadata = new java.util.HashMap<>();
		
		if (Constants.MEASUREMENT_TYPE_PROJECTION.equals(measurementTypeItem.getValue())) {
			TaskJsonItem scenarioItem = taskJson.getItems().stream().filter(x -> x.getKey().equals(Constants.MEASUREMENT_COD_SCENARIO)).findFirst().get();
			TaskJsonItem simulationSourceItem = taskJson.getItems().stream().filter(x -> x.getKey().equals(Constants.MEASUREMENT_SOURCE)).findFirst().get();

			metadata.put(MeasurementAttributeEnum.MEASURE_TYPE.name(), Constants.MEASUREMENT_TYPE_PROJECTION);
			metadata.put(MeasurementAttributeEnum.MEASURE_SIMULATION_COD_SCENARIO.name(), scenarioItem.getValue());
			metadata.put(MeasurementAttributeEnum.MEASURE_SIMULATION_SOURCE.name(), simulationSourceItem.getValue());

		} else {
			metadata.put(MeasurementAttributeEnum.MEASURE_TYPE.name(), Constants.MEASUREMENT_TYPE_ACTUAL);
			
			if (avgTermicTemp != null) {
				metadata.put(MeasurementAttributeEnum.TERMIC_AVG.name(), avgTermicTemp);
			}

		}
		
		metadata.put(MeasurementAttributeEnum.FILE_PATH.name(), objectKeyItem.getValue());
		metadata.put(MeasurementAttributeEnum.ID_BATCH.name(), taskId);
		
		measurement.setMetadata(metadata);
		
		return measurement;
	}
	
    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
    

    


}
