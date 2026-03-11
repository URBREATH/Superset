package eu.urbreathdsjobs.job;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JobScheduler {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JobScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job trafficJob;
    private final Job temperatureImportJob;
    private final JobExplorer jobExplorer;

    // Un lock per job
    private final Lock trafficLock = new ReentrantLock();
    private final Lock temperatureLock = new ReentrantLock();

    @Scheduled(cron = "0 0/5 * * * *")
    public void runTrafficJob() throws Exception {
        runJob(trafficJob, trafficLock, 1L);
    }

    @Scheduled(cron = "0 0/5 * * * *")
    public void runTemperatureImportJob() throws Exception {
        runJob(temperatureImportJob, temperatureLock, 2L);
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

