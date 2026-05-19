package eu.urbreathdsjobs.config;

import eu.urbreathdsjobs.listener.JobStatusListener;
import eu.urbreathdsjobs.listener.TaskAvailabilityDecider;
import eu.urbreathdsjobs.model.BatchJobTask;
import eu.urbreathdsjobs.model.TrafficCsvRow;
import eu.urbreathdsjobs.model.TrafficMeasurement;
import eu.urbreathdsjobs.processor.TaskQueueProcessor;
import eu.urbreathdsjobs.processor.TrafficProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class TrafficDataImportJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	// step: checkBatchTaskQueue reader/processor/writer
	

	// step:importTrafficDataStep reader/processor/writer
	private final FlatFileItemReader<TrafficCsvRow> csvTrafficReader;
	private final TrafficProcessor trafficProcessor;
	private final CompositeItemWriter<TrafficMeasurement> compositeTrafficItemWriter;
	
	private final JdbcCursorItemReader<BatchJobTask> jdbcTaskQueueReader;
	private final TaskQueueProcessor taskProcessor;
	private final JdbcBatchItemWriter<BatchJobTask> jdbcTaskQueueWriter;
	
	private final JobStatusListener jobStatusListener;
	
	private final TaskAvailabilityDecider decider;

	@Bean
	public Step taskQueueTrafficImportStep() {

		return new StepBuilder("taskQueueTrafficImportStep", jobRepository)
				.<BatchJobTask, BatchJobTask>chunk(1, transactionManager)
				.reader(jdbcTaskQueueReader)
				.processor(taskProcessor)
				.writer(jdbcTaskQueueWriter)
				.build();
	}

	@Bean
	public Step importTrafficDataStep() {
		return new StepBuilder("importTrafficDataStep", jobRepository)
				.<TrafficCsvRow, TrafficMeasurement>chunk(500, transactionManager)
				.reader(csvTrafficReader)
				.processor(trafficProcessor)
				.writer(compositeTrafficItemWriter)
				.build();
	}

	@Bean
	public Job trafficJob() {
		return new JobBuilder("trafficJob", jobRepository)
				.start(taskQueueTrafficImportStep())
	            .next(decider)
                	.on("TASKS_AVAILABLE").to(importTrafficDataStep())
                	.from(decider).on("NO_TASKS").end()
                 .end()
				.listener(jobStatusListener)
				.incrementer(new RunIdIncrementer()).build();
	}
}
