package eu.urbreathdsjobs.reader;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import eu.urbreathdsjobs.dto.TaskJson;
import eu.urbreathdsjobs.dto.TaskJsonItem;
import eu.urbreathdsjobs.model.TemperatureCsvRow;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.InvalidObjectStateException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;


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
//    	TaskJson taskJson = null;
//    	String taskId = null;
//    	
//    	if (jobExecution != null) {
//             taskJson = (TaskJson) jobExecution
//                    .get("TASK_JSON");
//                
//             taskId = jobExecution
//                    .getString("TASK_ID");
//    	} else {
//    		return null;
//    	}
//    	

        
        TaskJsonItem bucketItem = taskJson.getItems().stream().filter(x -> x.getKey().equals("bucket")).findFirst().get();
        TaskJsonItem objectKeyItem = taskJson.getItems().stream().filter(x -> x.getKey().equals("objectKey")).findFirst().get();

        
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

            if (fields[3].matches("-?\\d+(\\.\\d+)?") ) {
            	row.setTmax(Double.parseDouble(fields[3].trim()));
			} else {
				row.setTmax(null);
			}
            
            if (fields[4].matches("-?\\d+(\\.\\d+)?") ) {
            	row.setTmin(Double.parseDouble(fields[4].trim()));
            }	 else {
            	row.setTmin(null);
			}

            return row;
        });

        int currentItemCount = reader.getCurrentItemCount();


        return reader;
    }
    


}
