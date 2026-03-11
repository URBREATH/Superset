package eu.urbreathdsjobs.writer;

import javax.sql.DataSource;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import eu.urbreathdsjobs.model.BatchJobTask;
import eu.urbreathdsjobs.model.TrafficMeasurement;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskQueueWriter {
	
	private final DataSource dataSource;

	@Bean
	public JdbcBatchItemWriter<BatchJobTask> jdbcTaskQueueWriter() {

	    JdbcBatchItemWriter<BatchJobTask> writer =
	            new JdbcBatchItemWriter<>();

	    writer.setDataSource(dataSource);

	    writer.setSql("""
	        UPDATE batch_job_task_queue
	        SET status = 1,
	            date_mod = now()
	        WHERE id = :id
	    """);

        writer.setItemSqlParameterSourceProvider(
                new BeanPropertyItemSqlParameterSourceProvider<>());

	    return writer;
	}

}
