package eu.urbreathdsjobs.reader;

import eu.urbreathdsjobs.model.BatchJobTask;
import eu.urbreathdsjobs.service.TaskQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskQueueReader {

    private final TaskQueueService taskQueueService;

	@Bean
	@StepScope
	public ItemReader<BatchJobTask> jdbcTaskQueueReader(
			@Value("#{jobParameters['batch.id']}") Long batchId) {

        return new ItemReader<>() {
            private boolean consumed;

            @Override
            public BatchJobTask read() {
                if (consumed) {
                    return null;
                }
                consumed = true;
                return taskQueueService.getNextPendingTask(batchId);
            }
        };
	}

}
