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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class JobImportScheduler {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JobImportScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job trafficJob;

    private final Job temperatureImportJob;
    private final JobExplorer jobExplorer;
    private final Job precipitationImportJob;
    private final SchedulerControlService schedulerControlService;
    private final DynamicCronGateService dynamicCronGateService;

    // Un lock per job
    private final Lock trafficLock = new ReentrantLock();
    private final Lock temperatureLock = new ReentrantLock();
    private final Lock precipitationLock = new ReentrantLock();

    public JobImportScheduler(
            JobLauncher jobLauncher,
            @Qualifier("trafficJob") Job trafficJob,
            @Qualifier("temperatureImportJob") Job temperatureImportJob,
            @Qualifier("precipitationImportJob") Job precipitationImportJob,
            JobExplorer jobExplorer,
            SchedulerControlService schedulerControlService,
            DynamicCronGateService dynamicCronGateService) {
        this.jobLauncher = jobLauncher;
        this.trafficJob = trafficJob;
        this.temperatureImportJob = temperatureImportJob;
        this.precipitationImportJob = precipitationImportJob;
        this.jobExplorer = jobExplorer;
        this.schedulerControlService = schedulerControlService;
        this.dynamicCronGateService = dynamicCronGateService;
    }

    @Scheduled(fixedDelayString = "${backoffice.scheduler.tick-ms:1000}")
    public void runTrafficJob() throws Exception {
        if (!schedulerControlService.isEnabled(JobKey.TRAFFIC_IMPORT)) {
            return;
        }
        if (!dynamicCronGateService.shouldTrigger(JobKey.TRAFFIC_IMPORT)) {
            return;
        }
        runJob(trafficJob, trafficLock, 1L);
    }

    @Scheduled(fixedDelayString = "${backoffice.scheduler.tick-ms:1000}")
    public void runTemperatureImportJob() throws Exception {
        if (!schedulerControlService.isEnabled(JobKey.TEMPERATURE_IMPORT)) {
            return;
        }
        if (!dynamicCronGateService.shouldTrigger(JobKey.TEMPERATURE_IMPORT)) {
            return;
        }
        runJob(temperatureImportJob, temperatureLock, 2L);
    }

    @Scheduled(fixedDelayString = "${backoffice.scheduler.tick-ms:1000}")
    public void runPrecipitationImportJob() throws Exception {
        if (!schedulerControlService.isEnabled(JobKey.PRECIPITATION_IMPORT)) {
            return;
        }
        if (!dynamicCronGateService.shouldTrigger(JobKey.PRECIPITATION_IMPORT)) {
            return;
        }
        runJob(precipitationImportJob, precipitationLock, 3L);
    }

    private void runJob(Job job, Lock lock, Long batchID) throws Exception {
        if (!lock.tryLock()) {
            log.warn("{} already running, skipping...", job.getName());
            return;
        }
        try {
            if (isJobRunning(job)) {
                log.warn("{} already running in batch, skipping...", job.getName());
                return;
            }
            JobParameters params = new JobParametersBuilder()
            		.addLong("batch.id", batchID)
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, params);
        } finally {
            lock.unlock();
        }
    }
    
    private boolean isJobRunning(Job job) {
        return jobExplorer.findRunningJobExecutions(job.getName()).isEmpty() == false;
    }
}

