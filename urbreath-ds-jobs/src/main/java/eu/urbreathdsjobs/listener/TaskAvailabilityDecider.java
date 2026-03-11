package eu.urbreathdsjobs.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

import eu.urbreathdsjobs.dto.TaskJson;

@Component
public class TaskAvailabilityDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        TaskJson tasksCount = (TaskJson) jobExecution.getExecutionContext().get("TASK_JSON");
        if (tasksCount != null) {
            return new FlowExecutionStatus("TASKS_AVAILABLE");
        } else {
            return new FlowExecutionStatus("NO_TASKS");
        }
    }
}
