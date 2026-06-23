package eu.urbreathdsjobs.backoffice;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    public JobMonitorService(
            SchedulerControlService schedulerControlService,
            DynamicCronGateService dynamicCronGateService,
            JobExplorer jobExplorer
    ) {
        this.schedulerControlService = schedulerControlService;
        this.dynamicCronGateService = dynamicCronGateService;
        this.jobExplorer = jobExplorer;
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
                .filter(msg -> !msg.isBlank())
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
}


