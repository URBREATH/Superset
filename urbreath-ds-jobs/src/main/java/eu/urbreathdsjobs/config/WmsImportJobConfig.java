package eu.urbreathdsjobs.config;

import eu.urbreathdsjobs.tasklet.WmsImportTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class WmsImportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WmsImportTasklet wmsImportTasklet;

    @Bean
    public Step wmsImportStep() {
        return new StepBuilder("wmsImportStep", jobRepository)
                .tasklet(wmsImportTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job wmsImportJob() {
        return new JobBuilder("wmsImportJob", jobRepository)
                .start(wmsImportStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }
}

