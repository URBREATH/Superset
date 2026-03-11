package eu.urbreathdsjobs.reader;

import java.beans.PropertyEditorSupport;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import eu.urbreathdsjobs.dto.TaskJson;
import eu.urbreathdsjobs.dto.TaskJsonItem;
import eu.urbreathdsjobs.model.TrafficCsvRow;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Component
@RequiredArgsConstructor
public class TrafficCsvReader  {
	
	
	private final S3Client s3Client;
	

    @Bean
    @StepScope
    public FlatFileItemReader<TrafficCsvRow> csvTrafficReader(
            @Value("#{jobExecutionContext['TASK_JSON']}") TaskJson taskJson,
            @Value("#{jobExecutionContext['TASK_ID']}") String taskId
    		) {
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
        


        FlatFileItemReader<TrafficCsvRow> reader = new FlatFileItemReader<>();

        reader.setResource(new InputStreamResource(inputStream));
        reader.setLinesToSkip(1);

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames(
                "instance_id","segment_id","date","interval","uptime",
                "heavy","car","bike","pedestrian","night",
                "heavy_lft","heavy_rgt","car_lft","car_rgt",
                "bike_lft","bike_rgt","pedestrian_lft","pedestrian_rgt",
                "night_lft","night_rgt","direction",
                "car_speed_hist_0to70plus",
                "car_speed_hist_0to120plus",
                "timezone","v85"
        );

        BeanWrapperFieldSetMapper<TrafficCsvRow> mapper =
                new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(TrafficCsvRow.class);
        
        mapper.setCustomEditors(Map.of(
        	    LocalDateTime.class, new PropertyEditorSupport() {
        	        @Override
        	        public void setAsText(String text) {
        	            setValue(LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")));
        	        }
        	    }
        	));

        DefaultLineMapper<TrafficCsvRow> lineMapper =
                new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);

        reader.setLineMapper(lineMapper);

        return reader;
    }
}
