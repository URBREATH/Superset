package eu.urbreathdsjobs.dao;

import eu.urbreathdsjobs.model.BatchJobTask;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskQueueDao {

    private static final String SELECT_NEXT_PENDING_BY_BATCH = """
            SELECT id, id_batch, json_param, status, date_ins, date_mod
            FROM batch_job_task_queue
            WHERE status = 0
              AND id_batch = ?
            ORDER BY id
            LIMIT 1
            """;

    private static final String UPDATE_STATUS_IN_PROGRESS = """
            UPDATE batch_job_task_queue
            SET status = 1,
                date_mod = now()
            WHERE id = ?
            """;

    private static final String UPDATE_STATUS_AND_NOTE = """
            UPDATE batch_job_task_queue
            SET status = ?,
                note = ?,
                date_mod = now()
            WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public BatchJobTask findNextPendingByBatchId(Long batchId) {
        if (batchId == null) {
            return null;
        }

        return jdbcTemplate.query(
                SELECT_NEXT_PENDING_BY_BATCH,
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    BatchJobTask task = new BatchJobTask();
                    task.setId(rs.getLong("id"));
                    task.setIdBatch(rs.getInt("id_batch"));
                    task.setJsonParam(rs.getString("json_param"));
                    task.setStatus(rs.getInt("status"));
                    return task;
                },
                batchId
        );
    }

    public int markInProgress(Long taskId) {
        return jdbcTemplate.update(UPDATE_STATUS_IN_PROGRESS, taskId);
    }

    public int updateStatusAndNote(Long taskId, int status, String note) {
        return jdbcTemplate.update(UPDATE_STATUS_AND_NOTE, status, note, taskId);
    }
}

