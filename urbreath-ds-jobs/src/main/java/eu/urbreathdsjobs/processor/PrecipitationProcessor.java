package eu.urbreathdsjobs.processor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import eu.urbreathdsjobs.common.Constants;
import eu.urbreathdsjobs.common.MeasurementAttributeEnum;
import eu.urbreathdsjobs.dto.TaskJson;
import eu.urbreathdsjobs.dto.TaskJsonItem;
import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.model.MeasurementAttribute;
import eu.urbreathdsjobs.model.PrecipitationCsvRow;
import eu.urbreathdsjobs.model.TemperatureCsvRow;

@Component
public class PrecipitationProcessor  implements ItemProcessor<PrecipitationCsvRow, Measurement>, StepExecutionListener{
	
	private StepExecution stepExecution;
	


	

	@Override
	public Measurement process(PrecipitationCsvRow item) throws Exception {
		// TODO Auto-generated method stub
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
		measurement.setVal(item.getMmRain());

		List<MeasurementAttribute> attributes = new ArrayList<>();
		
		if (Constants.MEASUREMENT_TYPE_PROJECTION.equals(measurementTypeItem.getValue())) {
			TaskJsonItem scenarioItem = taskJson.getItems().stream().filter(x -> x.getKey().equals(Constants.MEASUREMENT_COD_SCENARIO)).findFirst().get();
			TaskJsonItem simulationSourceItem = taskJson.getItems().stream().filter(x -> x.getKey().equals(Constants.MEASUREMENT_SOURCE)).findFirst().get();

			
			attributes.add(createMeasurementAttribute(MeasurementAttributeEnum.MEASURE_TYPE,Constants.MEASUREMENT_TYPE_PROJECTION));
			attributes.add(createMeasurementAttribute(MeasurementAttributeEnum.MEASURE_SIMULATION_COD_SCENARIO, scenarioItem.getValue()));
			attributes.add(createMeasurementAttribute(MeasurementAttributeEnum.MEASURE_SIMULATION_SOURCE, simulationSourceItem.getValue()));

		} else {
			attributes.add(createMeasurementAttribute(MeasurementAttributeEnum.MEASURE_TYPE,Constants.MEASUREMENT_TYPE_ACTUAL));
			


		}
		
		attributes.add(createMeasurementAttribute(MeasurementAttributeEnum.FILE_PATH, objectKeyItem.getValue()));
		attributes.add(createMeasurementAttribute(MeasurementAttributeEnum.ID_BATCH, String.valueOf(taskId)));
		
		measurement.setAttributes(attributes);
		
		return measurement;
	}
	
    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
    
    private MeasurementAttribute createMeasurementAttribute(MeasurementAttributeEnum attributeEnum, String value) {
		MeasurementAttribute attribute = new MeasurementAttribute();
		attribute.setIdAttribute(attributeEnum.getIdAttribute());
		
		if (attributeEnum.getValueType().equals("NUMBER")) {
			attribute.setAttrValueNumber(Double.valueOf(value));
			attribute.setAttrValueString(null);
		} else {
			attribute.setAttrValueString(value);
			attribute.setAttrValueNumber(null);
		}
		
		
		return attribute;
	}
    


}
