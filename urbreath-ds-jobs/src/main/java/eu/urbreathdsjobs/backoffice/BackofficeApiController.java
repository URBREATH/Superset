package eu.urbreathdsjobs.backoffice;

import eu.urbreathdsjobs.service.TaskQueueService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/backoffice/api")
public class BackofficeApiController {

    private final JobMonitorService jobMonitorService;
    private final JobControlService jobControlService;
    private final SchedulerControlService schedulerControlService;
    private final DynamicCronGateService dynamicCronGateService;
    private final TaskQueueService taskQueueService;

    public BackofficeApiController(
            JobMonitorService jobMonitorService,
            JobControlService jobControlService,
            SchedulerControlService schedulerControlService,
            DynamicCronGateService dynamicCronGateService,
            TaskQueueService taskQueueService
    ) {
        this.jobMonitorService = jobMonitorService;
        this.jobControlService = jobControlService;
        this.schedulerControlService = schedulerControlService;
        this.dynamicCronGateService = dynamicCronGateService;
        this.taskQueueService = taskQueueService;
    }

    @GetMapping("/jobs")
    public List<JobMonitorService.JobStatusView> jobs() {
        return jobMonitorService.listJobs();
    }

    @GetMapping("/variables")
    public List<JobMonitorService.ScheduleVariableView> variables() {
        return jobMonitorService.schedulerVariables();
    }

    @PostMapping("/jobs/{jobId}/run")
    public JobControlService.ActionResult run(@PathVariable("jobId") String jobId) throws Exception {
        return jobControlService.runNow(parseKey(jobId));
    }

    @PostMapping("/jobs/{jobId}/stop")
    public JobControlService.ActionResult stop(@PathVariable("jobId") String jobId) throws Exception {
        return jobControlService.stopRunning(parseKey(jobId));
    }

    @PostMapping("/jobs/{jobId}/restart")
    public JobControlService.ActionResult restart(@PathVariable("jobId") String jobId) throws Exception {
        return jobControlService.restartLastFailedOrStopped(parseKey(jobId));
    }

    @PostMapping("/jobs/{jobId}/schedule")
    public Map<String, Object> updateSchedule(
            @PathVariable("jobId") String jobId,
            @RequestBody ScheduleUpdateRequest request
    ) {
        JobKey key = parseKey(jobId);
        SchedulerControlService.RuntimeSchedule updated = schedulerControlService.update(key, request.enabled(), request.cron());
        dynamicCronGateService.refresh(key);

        return Map.of(
                "success", true,
                "message", "Schedulazione aggiornata live (runtime). Ricordarsi di allineare anche file env/yaml per persistenza al restart.",
                "job", key.getId(),
                "enabled", updated.enabled(),
                "cron", updated.cron()
        );
    }

    @PostMapping("/jobs/{jobId}/queue/reset-stuck")
    public Map<String, Object> resetStuckQueue(
            @PathVariable("jobId") String jobId,
            @RequestParam(name = "minutes", defaultValue = "60") int minutes
    ) {
        JobKey key = parseKey(jobId);
        int safeMinutes = Math.max(minutes, 1);
        int resetRows = taskQueueService.resetStuckInProgressByBatchId(key.getBatchId(), safeMinutes);

        return Map.of(
                "success", true,
                "message", "Reset task queue completato",
                "job", key.getId(),
                "batchId", key.getBatchId(),
                "minutes", safeMinutes,
                "resetRows", resetRows
        );
    }

    private JobKey parseKey(String jobId) {
        return JobKey.fromPath(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job non valido: " + jobId));
    }

    public record ScheduleUpdateRequest(Boolean enabled, String cron) {
    }
}

