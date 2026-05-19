package eu.urbreathdsjobs.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.urbreathdsjobs.dto.TaskJson;
import eu.urbreathdsjobs.model.BatchJobTask;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TaskQueueProcessor implements ItemProcessor<BatchJobTask, BatchJobTask>,
                                          StepExecutionListener {

    private StepExecution stepExecution;

    @Override
    public BatchJobTask process(BatchJobTask item) throws JsonMappingException, JsonProcessingException {
    	
    	if (item.getJsonParam() != null && !item.getJsonParam().isEmpty()) {
    		ObjectMapper mapper = new ObjectMapper();
    		TaskJson taskJson = mapper.readValue(item.getJsonParam(), TaskJson.class);
            
    		stepExecution
            .getJobExecution()
            .getExecutionContext()
            .put("TASK_JSON", taskJson);
    	}
    	
        stepExecution
            .getJobExecution()
            .getExecutionContext()
            .putLong("TASK_ID", item.getId());

        return item;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
}

