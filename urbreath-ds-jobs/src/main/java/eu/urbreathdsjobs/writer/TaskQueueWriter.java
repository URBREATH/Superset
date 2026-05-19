package eu.urbreathdsjobs.writer;

import eu.urbreathdsjobs.model.BatchJobTask;
import eu.urbreathdsjobs.service.TaskQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskQueueWriter {

    private final TaskQueueService taskQueueService;

	@Bean
	public ItemWriter<BatchJobTask> jdbcTaskQueueWriter() {
        return new ItemWriter<>() {
            @Override
            public void write(Chunk<? extends BatchJobTask> chunk) {
                for (BatchJobTask task : chunk.getItems()) {
                    taskQueueService.markInProgress(task.getId());
                }
            }
        };
	}

}
