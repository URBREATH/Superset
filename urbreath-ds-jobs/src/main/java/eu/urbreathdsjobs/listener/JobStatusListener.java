package eu.urbreathdsjobs.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JobStatusListener implements JobExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void afterJob(JobExecution jobExecution) {
    	
    	if (jobExecution.getExecutionContext().containsKey("TASK_ID")) {
            Long taskId = jobExecution
                    .getExecutionContext()
                    .getLong("TASK_ID");

            int status = jobExecution.getStatus() == BatchStatus.COMPLETED
                    ? 2
                    : 9;

            jdbcTemplate.update("""
                UPDATE batch_job_task_queue
                SET status = ?,
                    date_mod = now()
                WHERE id = ?
            """, status, taskId);
    	}
    	

    }
}

