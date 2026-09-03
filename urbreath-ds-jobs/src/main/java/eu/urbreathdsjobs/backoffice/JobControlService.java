package eu.urbreathdsjobs.backoffice;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class JobControlService {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final Map<String, Job> jobsByBeanName;
    private final JdbcTemplate jdbcTemplate;

    public JobControlService(
            JobLauncher jobLauncher,
            JobExplorer jobExplorer,
            JobOperator jobOperator,
            Map<String, Job> jobsByBeanName,
            JdbcTemplate jdbcTemplate
    ) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.jobOperator = jobOperator;
        this.jobsByBeanName = jobsByBeanName;
        this.jdbcTemplate = jdbcTemplate;
    }

    public ActionResult runNow(JobKey key) throws Exception {
        Set<JobExecution> running = jobExplorer.findRunningJobExecutions(key.getJobName());
        if (!running.isEmpty()) {
            return new ActionResult(false, "Job gia in esecuzione, avvio manuale saltato");
        }

        Job job = jobsByBeanName.get(key.getJobName());
        if (job == null) {
            return new ActionResult(false, "Bean job non trovato: " + key.getJobName());
        }

        JobParameters parameters = new JobParametersBuilder()
                .addLong("batch.id", key.getBatchId())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(job, parameters);
        return new ActionResult(true, "Avvio manuale job inviato");
    }

    public ActionResult stopRunning(JobKey key) throws Exception {
        Set<JobExecution> running = jobExplorer.findRunningJobExecutions(key.getJobName());
        if (running.isEmpty()) {
            return new ActionResult(false, "Nessuna esecuzione in corso da fermare");
        }

        int stopped = 0;
        int alreadyStopping = 0;
        for (JobExecution execution : running) {
            try {
                if (jobOperator.stop(execution.getId())) {
                    stopped++;
                }
            } catch (JobExecutionNotRunningException ex) {
                alreadyStopping++;
            }
        }

        if (stopped > 0 && alreadyStopping > 0) {
            return new ActionResult(true, "Stop richiesto per " + stopped + " esecuzione(i); " + alreadyStopping + " gia in arresto");
        }
        if (stopped > 0) {
            return new ActionResult(true, "Stop richiesto per " + stopped + " esecuzione(i)");
        }
        if (alreadyStopping > 0) {
            return new ActionResult(true, "Le esecuzioni risultano gia in arresto (STOPPING): " + alreadyStopping);
        }
        return new ActionResult(false, "Nessuna esecuzione stoppabile trovata");
    }

    public ActionResult restartLastFailedOrStopped(JobKey key) throws Exception {
        Optional<JobExecution> candidate = lastRestartableExecution(key.getJobName());
        if (candidate.isEmpty()) {
            return new ActionResult(false, "Nessuna esecuzione FAILED/STOPPED trovata per restart");
        }

        Long executionId = candidate.get().getId();
        Long newExecutionId = jobOperator.restart(executionId);
        return new ActionResult(true, "Restart richiesto. Nuova execution id: " + newExecutionId);
    }

    private Optional<JobExecution> lastRestartableExecution(String jobName) {
        List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, 25);

        return instances.stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .filter(exec -> exec.getStatus() == BatchStatus.FAILED || exec.getStatus() == BatchStatus.STOPPED)
                .sorted(Comparator.comparing(JobExecution::getCreateTime).reversed())
                .findFirst();
    }

    public ForceCompleteResult forceCompleteStuck(JobKey key) {
        return forceCompleteStuckInternal(key != null ? key.getJobName() : null);
    }

    public ForceCompleteResult forceCompleteStuckAll() {
        return forceCompleteStuckInternal(null);
    }

    private ForceCompleteResult forceCompleteStuckInternal(String jobName) {
        LocalDateTime now = LocalDateTime.now();
        Timestamp nowTs = Timestamp.valueOf(now);
        String message = "Force complete da backoffice";

        int stepRows;
        int jobRows;
        if (jobName == null) {
            stepRows = jdbcTemplate.update("""
                    UPDATE public.batch_step_execution se
                    SET status = 'COMPLETED',
                        exit_code = 'COMPLETED',
                        exit_message = ?,
                        end_time = COALESCE(end_time, ?),
                        last_updated = ?
                    WHERE status IN ('STARTING', 'STARTED', 'STOPPING')
                    """, message, nowTs, nowTs);

            jobRows = jdbcTemplate.update("""
                    UPDATE public.batch_job_execution
                    SET status = 'COMPLETED',
                        exit_code = 'COMPLETED',
                        exit_message = ?,
                        end_time = COALESCE(end_time, ?),
                        last_updated = ?
                    WHERE status IN ('STARTING', 'STARTED', 'STOPPING')
                    """, message, nowTs, nowTs);
        } else {
            stepRows = jdbcTemplate.update("""
                    UPDATE public.batch_step_execution se
                    SET status = 'COMPLETED',
                        exit_code = 'COMPLETED',
                        exit_message = ?,
                        end_time = COALESCE(end_time, ?),
                        last_updated = ?
                    WHERE status IN ('STARTING', 'STARTED', 'STOPPING')
                      AND se.job_execution_id IN (
                        SELECT je.job_execution_id
                        FROM public.batch_job_execution je
                        JOIN public.batch_job_instance ji ON ji.job_instance_id = je.job_instance_id
                        WHERE ji.job_name = ?
                          AND je.status IN ('STARTING', 'STARTED', 'STOPPING')
                    )
                    """, message, nowTs, nowTs, jobName);

            jobRows = jdbcTemplate.update("""
                    UPDATE public.batch_job_execution je
                    SET status = 'COMPLETED',
                        exit_code = 'COMPLETED',
                        exit_message = ?,
                        end_time = COALESCE(end_time, ?),
                        last_updated = ?
                    WHERE je.status IN ('STARTING', 'STARTED', 'STOPPING')
                      AND je.job_instance_id IN (
                        SELECT ji.job_instance_id
                        FROM public.batch_job_instance ji
                        WHERE ji.job_name = ?
                    )
                    """, message, nowTs, nowTs, jobName);
        }

        return new ForceCompleteResult(true, "Aggiornamento forzato completato", jobRows, stepRows);
    }

    public record ActionResult(boolean success, String message) {
    }

    public record ForceCompleteResult(boolean success, String message, int jobExecutionRows, int stepExecutionRows) {
    }
}
