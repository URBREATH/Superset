package eu.urbreathdsjobs.backoffice;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobMonitorService {

    private static final int MAX_ERROR_LENGTH = 1200;

    private final SchedulerControlService schedulerControlService;
    private final DynamicCronGateService dynamicCronGateService;
    private final JobExplorer jobExplorer;
    private final JdbcTemplate jdbcTemplate;

    public JobMonitorService(
            SchedulerControlService schedulerControlService,
            DynamicCronGateService dynamicCronGateService,
            JobExplorer jobExplorer,
            JdbcTemplate jdbcTemplate
    ) {
        this.schedulerControlService = schedulerControlService;
        this.dynamicCronGateService = dynamicCronGateService;
        this.jobExplorer = jobExplorer;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<JobStatusView> listJobs() {
        List<JobStatusView> result = new ArrayList<>();

        for (JobKey key : JobKey.values()) {
            SchedulerControlService.RuntimeSchedule runtimeSchedule = schedulerControlService.get(key);
            Set<JobExecution> running = jobExplorer.findRunningJobExecutions(key.getJobName());
            JobExecution lastExecution = findLastExecution(key.getJobName());

            result.add(new JobStatusView(
                    key.getId(),
                    key.getDisplayName(),
                    key.getJobName(),
                    runtimeSchedule.enabled(),
                    runtimeSchedule.cron(),
                    dynamicCronGateService.getNextRun(key),
                    dynamicCronGateService.getSecondsToNextRun(key),
                    !running.isEmpty(),
                    running.stream().map(JobExecution::getId).sorted().toList(),
                    lastExecution != null ? lastExecution.getId() : null,
                    lastExecution != null ? lastExecution.getStatus().name() : "NEVER_RUN",
                    extractError(lastExecution),
                    lastExecution != null ? toInstant(lastExecution.getStartTime()) : null,
                    lastExecution != null ? toInstant(lastExecution.getEndTime()) : null
            ));
        }

        return result;
    }

    public List<ScheduleVariableView> schedulerVariables() {
        List<ScheduleVariableView> vars = new ArrayList<>();

        for (JobKey key : JobKey.values()) {
            SchedulerControlService.RuntimeSchedule runtimeSchedule = schedulerControlService.get(key);
            vars.add(new ScheduleVariableView(key.getEnabledProperty(), String.valueOf(runtimeSchedule.enabled())));
            vars.add(new ScheduleVariableView(key.getCronProperty(), runtimeSchedule.cron()));
        }

        return vars;
    }

    public JobObservabilityResponse observability(String fromDate, String toDate, boolean includeExecutions) {
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = LocalDate.parse(toDate);
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("toDate must be >= fromDate");
        }
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();
        List<JobExecutionSummary> summaries = jdbcTemplate.query("""
                SELECT ji.job_name,
                       COUNT(*) AS calls,
                       SUM(CASE WHEN je.status = 'COMPLETED' AND je.exit_code = 'COMPLETED' THEN 1 ELSE 0 END) AS positives,
                       SUM(CASE WHEN je.status = 'COMPLETED' AND je.exit_code = 'COMPLETED' THEN 0 ELSE 1 END) AS negatives,
                       SUM(CASE WHEN COALESCE(ws.inserted_count, 0) > 0 THEN 1 ELSE 0 END) AS with_inserts,
                       SUM(CASE WHEN COALESCE(ws.inserted_count, 0) > 0 THEN 0 ELSE 1 END) AS without_inserts,
                       COALESCE(SUM(COALESCE(ws.inserted_count, 0)), 0) AS total_inserted,
                       MIN(je.start_time) AS first_called_at,
                       MAX(je.start_time) AS last_called_at
                FROM public.batch_job_execution je
                JOIN public.batch_job_instance ji ON ji.job_instance_id = je.job_instance_id
                LEFT JOIN (
                    SELECT job_execution_id, COALESCE(SUM(COALESCE(write_count, 0)), 0) AS inserted_count
                    FROM public.batch_step_execution
                    GROUP BY job_execution_id
                ) ws ON ws.job_execution_id = je.job_execution_id
                WHERE je.start_time >= ?
                  AND je.start_time < ?
                GROUP BY ji.job_name
                ORDER BY ji.job_name
                """,
                (rs, rowNum) -> new JobExecutionSummary(
                        jobIdFromName(rs.getString("job_name")),
                        rs.getString("job_name"),
                        rs.getLong("calls"),
                        rs.getLong("positives"),
                        rs.getLong("negatives"),
                        rs.getLong("with_inserts"),
                        rs.getLong("without_inserts"),
                        rs.getLong("total_inserted"),
                        toInstant(rs.getTimestamp("first_called_at") != null ? rs.getTimestamp("first_called_at").toLocalDateTime() : null),
                        toInstant(rs.getTimestamp("last_called_at") != null ? rs.getTimestamp("last_called_at").toLocalDateTime() : null)
                ),
                java.sql.Timestamp.valueOf(fromTime),
                java.sql.Timestamp.valueOf(toExclusive)
        );

        List<ExecutionAnalyticsRow> rows = includeExecutions ? loadExecutionRows(fromTime, toExclusive) : List.of();

        return new JobObservabilityResponse(
                "custom",
                fromTime.atZone(java.time.ZoneId.systemDefault()).toInstant(),
                toExclusive.atZone(java.time.ZoneId.systemDefault()).toInstant(),
                summaries,
                rows
        );
    }

    private List<ExecutionAnalyticsRow> loadExecutionRows(LocalDateTime fromTime, LocalDateTime toExclusive) {
        return jdbcTemplate.query("""
                SELECT ji.job_name,
                       je.job_execution_id,
                       je.start_time,
                       je.end_time,
                       je.status,
                       je.exit_code,
                       COALESCE(SUM(COALESCE(se.write_count, 0)), 0) AS inserted_count
                FROM public.batch_job_execution je
                JOIN public.batch_job_instance ji ON ji.job_instance_id = je.job_instance_id
                LEFT JOIN public.batch_step_execution se ON se.job_execution_id = je.job_execution_id
                WHERE je.start_time >= ?
                  AND je.start_time < ?
                GROUP BY ji.job_name, je.job_execution_id, je.start_time, je.end_time, je.status, je.exit_code
                ORDER BY je.start_time DESC
                """,
                (rs, rowNum) -> {
                    long insertedCount = rs.getLong("inserted_count");
                    return new ExecutionAnalyticsRow(
                            jobIdFromName(rs.getString("job_name")),
                            rs.getString("job_name"),
                            rs.getLong("job_execution_id"),
                            toInstant(rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null),
                            toInstant(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null),
                            rs.getString("status"),
                            rs.getString("exit_code"),
                            insertedCount,
                            insertedCount > 0
                    );
                },
                java.sql.Timestamp.valueOf(fromTime),
                java.sql.Timestamp.valueOf(toExclusive)
        );
    }

    private String jobIdFromName(String jobName) {
        for (JobKey key : JobKey.values()) {
            if (key.getJobName().equals(jobName)) {
                return key.getId();
            }
        }
        return jobName;
    }

    private JobExecution findLastExecution(String jobName) {
        // Read only the latest instance to keep UI refresh fast on large batch metadata tables.
        List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, 1);
        return instances.stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .sorted(Comparator.comparing(JobExecution::getCreateTime).reversed())
                .findFirst()
                .orElse(null);
    }

    private String extractError(JobExecution execution) {
        if (execution == null) {
            return null;
        }

        if (execution.getStatus() == BatchStatus.COMPLETED) {
            return null;
        }

        String failure = execution.getAllFailureExceptions().stream()
                .map(Throwable::getMessage)
                .filter(msg -> msg != null && !msg.isBlank())
                .collect(Collectors.joining(" | "));

        if (!failure.isBlank()) {
            return truncate(failure);
        }

        String stepError = execution.getStepExecutions().stream()
                .map(StepExecution::getExitStatus)
                .map(exit -> exit.getExitDescription())
                .filter(msg -> msg != null && !msg.isBlank())
                .collect(Collectors.joining(" | "));

        return stepError.isBlank() ? null : truncate(stepError);
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_ERROR_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_ERROR_LENGTH) + "...";
    }

    private Instant toInstant(java.time.LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();
    }

    public record JobStatusView(
            String id,
            String displayName,
            String jobName,
            boolean enabled,
            String cron,
            Instant nextRunAt,
            long secondsToNextRun,
            boolean running,
            List<Long> runningExecutionIds,
            Long lastExecutionId,
            String lastStatus,
            String lastError,
            Instant lastStartAt,
            Instant lastEndAt
    ) {
    }

    public record ScheduleVariableView(String key, String value) {
    }

    public record JobObservabilityResponse(
            String period,
            Instant from,
            Instant to,
            List<JobExecutionSummary> summaries,
            List<ExecutionAnalyticsRow> executions
    ) {
    }

    public record JobExecutionSummary(
            String jobId,
            String jobName,
            long calls,
            long positives,
            long negatives,
            long withInserts,
            long withoutInserts,
            long totalInserted,
            Instant firstCalledAt,
            Instant lastCalledAt
    ) {
    }

    public record ExecutionAnalyticsRow(
            String jobId,
            String jobName,
            long executionId,
            Instant calledAt,
            Instant endedAt,
            String status,
            String exitCode,
            long insertedCount,
            boolean inserted
    ) {
    }

}
