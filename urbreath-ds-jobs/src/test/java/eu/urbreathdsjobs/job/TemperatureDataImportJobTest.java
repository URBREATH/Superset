package eu.urbreathdsjobs.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import eu.urbreathdsjobs.launcher.App;




//@ActiveProfiles("dev") 
@SpringBootTest(classes = App.class)
class TemperatureDataImportJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job temperatureImportJob;

    @Test
    void runRealJob() throws Exception {

        JobParameters params = new JobParametersBuilder()
//                .addString("fileName", "anagrafica.csv")
                .addLong("batch.id", 2L)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(temperatureImportJob, params);

        assertEquals(ExitStatus.COMPLETED.getExitCode(), execution.getExitStatus().getExitCode());
    }
}
