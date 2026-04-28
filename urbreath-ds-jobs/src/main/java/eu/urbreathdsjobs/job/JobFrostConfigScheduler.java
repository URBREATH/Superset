package eu.urbreathdsjobs.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
public class JobFrostConfigScheduler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JobFrostConfigScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job frostConfigJob;
    private final JobExplorer jobExplorer;

    @Value("${app.scheduler.frost.config.enabled:true}")
    private boolean frostConfigEnabled;

    private final Lock frostConfigLock = new ReentrantLock();

    public JobFrostConfigScheduler(
            JobLauncher jobLauncher,
            @Qualifier("frostConfigJob") Job frostConfigJob,
            JobExplorer jobExplorer
    ) {
        this.jobLauncher = jobLauncher;
        this.frostConfigJob = frostConfigJob;
        this.jobExplorer = jobExplorer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runFrostConfigJobOnStartup() {
        try {
            log.info("Application ready, launching {} on startup...", frostConfigJob.getName());
            runFrostConfigJob();
        } catch (Exception ex) {
            log.error("Unable to launch {} on startup", frostConfigJob.getName(), ex);
        }
    }

    @Scheduled(cron = "${app.scheduler.frost.config.cron:0 0/20 * * * *}")
    public void runFrostConfigJob() throws Exception {
        if (!frostConfigEnabled) {
            return;
        }
        if (!frostConfigLock.tryLock()) {
            log.warn("{} already running in scheduler lock, skipping...", frostConfigJob.getName());
            return;
        }

        try {
            if (isJobRunning(frostConfigJob)) {
                log.warn("{} already running in batch metadata, skipping...", frostConfigJob.getName());
                return;
            }

            JobParameters params = new JobParametersBuilder()
                    .addLong("batch.id", 6L)
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(frostConfigJob, params);
        } finally {
            frostConfigLock.unlock();
        }
    }

    private boolean isJobRunning(Job job) {
        return !jobExplorer.findRunningJobExecutions(job.getName()).isEmpty();
    }
}

