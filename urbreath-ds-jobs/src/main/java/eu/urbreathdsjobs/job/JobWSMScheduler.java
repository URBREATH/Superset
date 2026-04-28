package eu.urbreathdsjobs.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.scheduler.wms.enabled:true}")
    private boolean wmsEnabled;

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


    @Scheduled(cron = "${app.scheduler.wms.cron:0 0/20 * * * *}")
    public void runWmsImportJob() throws Exception {
        if (!wmsEnabled) {
            return;
        }
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

