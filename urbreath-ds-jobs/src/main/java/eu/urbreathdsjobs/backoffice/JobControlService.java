package eu.urbreathdsjobs.backoffice;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

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

    public JobControlService(
            JobLauncher jobLauncher,
            JobExplorer jobExplorer,
            JobOperator jobOperator,
            Map<String, Job> jobsByBeanName
    ) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.jobOperator = jobOperator;
        this.jobsByBeanName = jobsByBeanName;
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
        for (JobExecution execution : running) {
            if (jobOperator.stop(execution.getId())) {
                stopped++;
            }
        }

        return new ActionResult(stopped > 0, "Stop richiesto per " + stopped + " esecuzione(i)");
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

    public record ActionResult(boolean success, String message) {
    }
}

