package eu.urbreathdsjobs.config;

import eu.urbreathdsjobs.tasklet.FrostConfigTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
@ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
public class FrostConfigJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FrostConfigTasklet frostConfigTasklet;

    @Bean
    public Step frostConfigStep() {
        return new StepBuilder("frostConfigStep", jobRepository)
                .tasklet(frostConfigTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job frostConfigJob() {
        return new JobBuilder("frostConfigJob", jobRepository)
                .start(frostConfigStep())
                .incrementer(new RunIdIncrementer())
                .build();
    }
}

