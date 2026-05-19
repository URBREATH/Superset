package eu.urbreathdsjobs.config;

import eu.urbreathdsjobs.listener.JobStatusListener;
import eu.urbreathdsjobs.listener.TaskAvailabilityDecider;
import eu.urbreathdsjobs.model.BatchJobTask;
import eu.urbreathdsjobs.model.Measurement;
import eu.urbreathdsjobs.model.PrecipitationCsvRow;
import eu.urbreathdsjobs.processor.PrecipitationProcessor;
import eu.urbreathdsjobs.processor.TaskQueueProcessor;
import eu.urbreathdsjobs.writer.MeasurementWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class PrecipitationDataImportJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	// step: checkBatchTaskQueue reader/processor/writer
	

	// step:importTrafficDataStep reader/processor/writer
	private final FlatFileItemReader<PrecipitationCsvRow> csvPrecipitationReader;
	private final PrecipitationProcessor precipitationProcessor;
	private final MeasurementWriter measurementItemWriter;
	
	private final ItemReader<BatchJobTask> jdbcTaskQueueReader;
	private final TaskQueueProcessor taskProcessor;
	private final ItemWriter<BatchJobTask> jdbcTaskQueueWriter;

	private final JobStatusListener jobStatusListener;
	
	private final TaskAvailabilityDecider decider;

	@Bean
	public Step taskQueuePrecipitationImportStep() {

		return new StepBuilder("taskQueuePrecipitationImportStep", jobRepository)
				.<BatchJobTask, BatchJobTask>chunk(1, transactionManager)
				.reader(jdbcTaskQueueReader)
				.processor(taskProcessor)
				.writer(jdbcTaskQueueWriter)
				.build();
	}

	@Bean
	public Step precipitationImportStep() {
		return new StepBuilder("precipitationImportStep", jobRepository)
				.<PrecipitationCsvRow,Measurement>chunk(1000, transactionManager)
				.reader(csvPrecipitationReader)
				.processor(precipitationProcessor)
				.writer(measurementItemWriter)
				.build();
	}

	@Bean
	public Job precipitationImportJob() {
		return new JobBuilder("precipitationImportJob", jobRepository)
				.start(taskQueuePrecipitationImportStep())
	            .next(decider)
                	.on("TASKS_AVAILABLE").to(precipitationImportStep())
                	.from(decider).on("NO_TASKS").end()
                 .end()
				.listener(jobStatusListener)
				.incrementer(new RunIdIncrementer()).build();
	}
}
