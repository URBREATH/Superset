package eu.urbreathdsjobs.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class JobWSMScheduler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JobWSMScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job wmsImportJob;
    private final JobExplorer jobExplorer;

    private final Lock wmsLock = new ReentrantLock();

    public JobWSMScheduler(
            JobLauncher jobLauncher,
            @Qualifier("wmsImportJob") Job wmsImportJob,
            JobExplorer jobExplorer
    ) {
        this.jobLauncher = jobLauncher;
        this.wmsImportJob = wmsImportJob;
        this.jobExplorer = jobExplorer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runWmsImportJobOnStartup() {
        try {
            log.info("Application ready, launching {} on startup...", wmsImportJob.getName());
            runWmsImportJob();
        } catch (Exception ex) {
            log.error("Unable to launch {} on startup", wmsImportJob.getName(), ex);
        }
    }

    @Scheduled(
            initialDelayString = "${app.scheduler.wms.initial-delay-ms:30000}",
            fixedRateString = "${app.scheduler.wms.fixed-rate-ms:120000}"
    )
    public void runWmsImportJob() throws Exception {
        if (!wmsLock.tryLock()) {
            log.warn("{} already running in scheduler lock, skipping...", wmsImportJob.getName());
            return;
        }

        try {
            if (isJobRunning(wmsImportJob)) {
                log.warn("{} already running in batch metadata, skipping...", wmsImportJob.getName());
                return;
            }

            JobParameters params = new JobParametersBuilder()
                    .addLong("batch.id", 4L)
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(wmsImportJob, params);
        } finally {
            wmsLock.unlock();
        }
    }

    private boolean isJobRunning(Job job) {
        return !jobExplorer.findRunningJobExecutions(job.getName()).isEmpty();
    }
}

