package eu.urbreathdsjobs.reader;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import eu.urbreathdsjobs.model.BatchJobTask;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskQueueReader {
	
	private final DataSource dataSource;

	@Bean
	@StepScope
	public JdbcCursorItemReader<BatchJobTask> jdbcTaskQueueReader(
			@Value("#{jobParameters['batch.id']}") Long batchId) {

	    JdbcCursorItemReader<BatchJobTask> reader = new JdbcCursorItemReader<>();
	    reader.setDataSource(dataSource);

	    reader.setSql("""
	            SELECT *
	            FROM batch_job_task_queue
	            WHERE status = 0
	            AND id_batch = ?
	            ORDER BY id
	            LIMIT 1
	        """);

	        reader.setPreparedStatementSetter(ps -> ps.setLong(1, batchId));
	    

	    reader.setRowMapper((rs, rowNum) -> {
	        BatchJobTask t = new BatchJobTask();
	        t.setId(rs.getLong("id"));
	        t.setIdBatch(rs.getInt("id_batch"));
	        t.setJsonParam(rs.getString("json_param"));
	        t.setStatus(rs.getInt("status"));
	        return t;
	    });

	    return reader;
	}

}
