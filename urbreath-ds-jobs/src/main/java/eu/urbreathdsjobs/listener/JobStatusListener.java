package eu.urbreathdsjobs.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import eu.urbreathdsjobs.service.TaskQueueService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobStatusListener implements JobExecutionListener {

    private final TaskQueueService taskQueueService;

    @Override
    public void afterJob(JobExecution jobExecution) {
    	
    	if (jobExecution.getExecutionContext().containsKey("TASK_ID")) {
            Long taskId = jobExecution
                    .getExecutionContext()
                    .getLong("TASK_ID");
            


            int status = jobExecution.getStatus() == BatchStatus.COMPLETED
                    ? 2
                    : 9;  
            
            StringBuilder sb = new StringBuilder("");
            jobExecution.getStepExecutions().stream()
            .filter(s -> !(s.getStepName().contains("taskQueue"))) // nome del tuo step
            .findFirst()
            .ifPresent(s -> {
            	sb.append("Righe lette: " + s.getReadCount() + "\n");
            	sb.append("Righe saltate: " + s.getSkipCount()+ "\n");
            	sb.append("Righe scritte: " + s.getWriteCount()+ "\n");
            	sb.append("Righe fallite: " + s.getReadSkipCount()+ "\n");
            });

            taskQueueService.updateStatusAndNote(taskId, status, sb.toString());
    	}
    	

    }
}

