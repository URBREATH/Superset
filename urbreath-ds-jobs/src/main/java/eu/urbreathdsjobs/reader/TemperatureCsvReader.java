package eu.urbreathdsjobs.reader;

import java.io.InputStream;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import eu.urbreathdsjobs.common.Constants;
import eu.urbreathdsjobs.common.MeasurementAttributeEnum;
import eu.urbreathdsjobs.dto.TaskJson;
import eu.urbreathdsjobs.dto.TaskJsonItem;
import eu.urbreathdsjobs.model.TemperatureCsvRow;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;


@Component
@RequiredArgsConstructor
public class TemperatureCsvReader  {
	
	private final S3Client s3Client;

	
    @Bean
    @StepScope
    public FlatFileItemReader<TemperatureCsvRow> csvTemperatureReader(
            @Value("#{jobExecutionContext['TASK_JSON']}") TaskJson taskJson,
            @Value("#{jobExecutionContext['TASK_ID']}") String taskId
    		)  {
       
        TaskJsonItem bucketItem = taskJson.getItems().stream().filter(x -> x.getKey().equals("bucket")).findFirst().get();
        TaskJsonItem objectKeyItem = taskJson.getItems().stream().filter(x -> x.getKey().equals("objectKey")).findFirst().get();
		TaskJsonItem measurementTypeItem = taskJson.getItems().stream().filter(x -> x.getKey().equals(MeasurementAttributeEnum.MEASURE_TYPE.name())).findFirst().get();

        
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketItem.getValue())
                .key(objectKeyItem.getValue())
                .build();

        InputStream inputStream =  s3Client.getObject(request);

        FlatFileItemReader<TemperatureCsvRow> reader = new FlatFileItemReader<>();

        reader.setResource(new InputStreamResource(inputStream));

        reader.setLinesToSkip(0);
        reader.setLineMapper((line, lineNumber) -> {
            String[] fields = line.split("\t");
            TemperatureCsvRow row = new TemperatureCsvRow();
            row.setYear(Integer.parseInt(fields[0].trim()));
            row.setMonth(Integer.parseInt(fields[1].trim()));
            row.setDay(Integer.parseInt(fields[2].trim()));
            
            Double field3 = null;
            Double field4 = null;
            if (fields[3].matches("-?\\d+(\\.\\d+)?") ) {
            	field3 = Double.parseDouble(fields[3].trim());
			}
            
            if (fields[4].matches("-?\\d+(\\.\\d+)?") ) {
            	field4 = Double.parseDouble(fields[4].trim());
            }
            

            if (Constants.MEASUREMENT_TYPE_PROJECTION.equals(measurementTypeItem.getValue())) {
				row.setTmax(field4);
				row.setTmin(field3);
			} else {
				row.setTmax(field3);
				row.setTmin(field4);
			}

            return row;
        });

        int currentItemCount = reader.getCurrentItemCount();


        return reader;
    }
    


}
