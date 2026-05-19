package eu.urbreathdsjobs.reader;

import eu.urbreathdsjobs.dto.TaskJson;
import eu.urbreathdsjobs.dto.TaskJsonItem;
import eu.urbreathdsjobs.model.PrecipitationCsvRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;


@Component
@RequiredArgsConstructor
public class PrecipitationCsvReader  {
	
	private final S3Client s3Client;

	
    @Bean
    @StepScope
    public FlatFileItemReader<PrecipitationCsvRow> csvPrecipitationReader(
            @Value("#{jobExecutionContext['TASK_JSON']}") TaskJson taskJson,
            @Value("#{jobExecutionContext['TASK_ID']}") String taskId
    		)  {

       
        TaskJsonItem bucketItem = taskJson.getItems().stream().filter(x -> x.getKey().equals("bucket")).findFirst().get();
        TaskJsonItem objectKeyItem = taskJson.getItems().stream().filter(x -> x.getKey().equals("objectKey")).findFirst().get();

        
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketItem.getValue())
                .key(objectKeyItem.getValue())
                .build();

        InputStream inputStream =  s3Client.getObject(request);

        FlatFileItemReader<PrecipitationCsvRow> reader = new FlatFileItemReader<>();

        reader.setResource(new InputStreamResource(inputStream));
        reader.setLinesToSkip(1);
        reader.setLineMapper((line, lineNumber) -> {
            String[] fields = line.split("\t");
            PrecipitationCsvRow row = new PrecipitationCsvRow();
            row.setYear(Integer.parseInt(fields[0].trim()));
            row.setMonth(Integer.parseInt(fields[1].trim()));
            row.setDay(Integer.parseInt(fields[2].trim()));

            if (fields[3].matches("-?\\d+(\\.\\d+)?") ) {
            	row.setMmRain(Double.parseDouble(fields[3].trim()));
			} else {
				row.setMmRain(null);
			}
            


            return row;
        });

 

        return reader;
    }
    


}
