package eu.urbreathdsjobs.job;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobImportScheduler {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JobImportScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job trafficJob;

    private final Job temperatureImportJob;
    private final JobExplorer jobExplorer;
    private final Job precipitationImportJob;

    // Un lock per job
    private final Lock trafficLock = new ReentrantLock();
    private final Lock temperatureLock = new ReentrantLock();
    private final Lock precipitationLock = new ReentrantLock();

    @Value("${app.scheduler.import.traffic.enabled:true}")
    private boolean trafficEnabled;

    @Value("${app.scheduler.import.temperature.enabled:true}")
    private boolean temperatureEnabled;

    @Value("${app.scheduler.import.precipitation.enabled:true}")
    private boolean precipitationEnabled;
    
    public JobImportScheduler(
            JobLauncher jobLauncher,
            @Qualifier("trafficJob") Job trafficJob,
            @Qualifier("temperatureImportJob") Job temperatureImportJob,
            @Qualifier("precipitationImportJob") Job precipitationImportJob,
            JobExplorer jobExplorer) {
        this.jobLauncher = jobLauncher;
        this.trafficJob = trafficJob;
        this.temperatureImportJob = temperatureImportJob;
        this.precipitationImportJob = precipitationImportJob;
        this.jobExplorer = jobExplorer;
    }

    @Scheduled(cron = "${app.scheduler.import.traffic.cron:0/30 * * * * *}")
    public void runTrafficJob() throws Exception {
        if (!trafficEnabled) {
            return;
        }
        runJob(trafficJob, trafficLock, 1L);
    }

    @Scheduled(cron = "${app.scheduler.import.temperature.cron:0/30 * * * * *}")
    public void runTemperatureImportJob() throws Exception {
        if (!temperatureEnabled) {
            return;
        }
        runJob(temperatureImportJob, temperatureLock, 2L);
    }

    @Scheduled(cron = "${app.scheduler.import.precipitation.cron:0/30 * * * * *}")
    public void runPrecipitationImportJob() throws Exception {
        if (!precipitationEnabled) {
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

