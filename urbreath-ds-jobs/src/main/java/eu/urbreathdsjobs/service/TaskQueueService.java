package eu.urbreathdsjobs.service;

import eu.urbreathdsjobs.dao.TaskQueueDao;
import eu.urbreathdsjobs.model.BatchJobTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class TaskQueueService {

    private final TaskQueueDao taskQueueDao;

    public BatchJobTask getNextPendingTask(Long batchId) {
        return taskQueueDao.findNextPendingByBatchId(batchId);
    }

    public void markInProgress(Long taskId) {
        taskQueueDao.markInProgress(taskId);
    }

    public void updateStatusAndNote(Long taskId, int status, String note) {
        taskQueueDao.updateStatusAndNote(taskId, status, note);
    }

    public int resetStuckInProgressByBatchId(Long batchId, int olderThanMinutes) {
        return taskQueueDao.resetStuckInProgressByBatchId(batchId, olderThanMinutes);
    }
}

