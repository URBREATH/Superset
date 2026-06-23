package eu.urbreathdsjobs.job;

import eu.urbreathdsjobs.backoffice.DynamicCronGateService;
import eu.urbreathdsjobs.backoffice.JobKey;
import eu.urbreathdsjobs.backoffice.SchedulerControlService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
public class JobFrostImportScheduler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JobFrostImportScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job frostImportJob;
    private final JobExplorer jobExplorer;
    private final SchedulerControlService schedulerControlService;
    private final DynamicCronGateService dynamicCronGateService;

    private final Lock frostImportLock = new ReentrantLock();

    public JobFrostImportScheduler(
            JobLauncher jobLauncher,
            @Qualifier("frostImportJob") Job frostImportJob,
            JobExplorer jobExplorer,
            SchedulerControlService schedulerControlService,
            DynamicCronGateService dynamicCronGateService
    ) {
        this.jobLauncher = jobLauncher;
        this.frostImportJob = frostImportJob;
        this.jobExplorer = jobExplorer;
        this.schedulerControlService = schedulerControlService;
        this.dynamicCronGateService = dynamicCronGateService;
    }


    @Scheduled(fixedDelayString = "${backoffice.scheduler.tick-ms:1000}")
    public void runFrostImportJob() throws Exception {
        if (!schedulerControlService.isEnabled(JobKey.FROST_IMPORT)) {
            return;
        }
        if (!dynamicCronGateService.shouldTrigger(JobKey.FROST_IMPORT)) {
            return;
        }
        if (!frostImportLock.tryLock()) {
            log.warn("{} already running in scheduler lock, skipping...", frostImportJob.getName());
            return;
        }

        try {
            if (isJobRunning(frostImportJob)) {
                log.warn("{} already running in batch metadata, skipping...", frostImportJob.getName());
                return;
            }

            JobParameters params = new JobParametersBuilder()
                    .addLong("batch.id", 5L)
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(frostImportJob, params);
        } finally {
            frostImportLock.unlock();
        }
    }

    private boolean isJobRunning(Job job) {
        return !jobExplorer.findRunningJobExecutions(job.getName()).isEmpty();
    }
}

