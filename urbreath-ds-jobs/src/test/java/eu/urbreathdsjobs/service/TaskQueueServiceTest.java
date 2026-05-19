package eu.urbreathdsjobs.service;

import eu.urbreathdsjobs.dao.TaskQueueDao;
import eu.urbreathdsjobs.model.BatchJobTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskQueueService tests")
class TaskQueueServiceTest {

	@Mock
	private TaskQueueDao taskQueueDao;

	@InjectMocks
	private TaskQueueService taskQueueService;

	@Test
	void getNextPendingTaskDelegatesAndReturnsDaoResult() {
		Long batchId = 7L;
		BatchJobTask expected = new BatchJobTask();
		expected.setId(99L);

		when(taskQueueDao.findNextPendingByBatchId(batchId)).thenReturn(expected);

		BatchJobTask actual = taskQueueService.getNextPendingTask(batchId);

		assertSame(expected, actual);
		verify(taskQueueDao, times(1)).findNextPendingByBatchId(batchId);
	}

	@Test
	void markInProgressDelegatesToDao() {
		Long taskId = 11L;

		taskQueueService.markInProgress(taskId);

		verify(taskQueueDao, times(1)).markInProgress(taskId);
	}

	@Test
	void updateStatusAndNoteDelegatesToDao() {
		Long taskId = 12L;
		int status = 2;
		String note = "ok";

		taskQueueService.updateStatusAndNote(taskId, status, note);

		verify(taskQueueDao, times(1)).updateStatusAndNote(taskId, status, note);
	}
}

