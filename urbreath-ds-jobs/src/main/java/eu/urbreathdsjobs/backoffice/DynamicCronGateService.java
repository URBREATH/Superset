package eu.urbreathdsjobs.backoffice;

import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
public class DynamicCronGateService {

    private final SchedulerControlService schedulerControlService;
    private final Map<JobKey, GateState> states = new EnumMap<>(JobKey.class);

    public DynamicCronGateService(SchedulerControlService schedulerControlService) {
        this.schedulerControlService = schedulerControlService;
    }

    public synchronized boolean shouldTrigger(JobKey key) {
        if (!schedulerControlService.isEnabled(key)) {
            return false;
        }

        GateState state = ensureState(key);
        Instant now = Instant.now();

        if (state.nextRun() == null || !now.isBefore(state.nextRun())) {
            state = computeNextState(key, state.cron(), ZonedDateTime.now().plusNanos(1));
            states.put(key, state);
            return true;
        }

        return false;
    }

    public synchronized Instant getNextRun(JobKey key) {
        return ensureState(key).nextRun();
    }

    public synchronized long getSecondsToNextRun(JobKey key) {
        if (!schedulerControlService.isEnabled(key)) {
            return -1L;
        }

        Instant next = getNextRun(key);
        if (next == null) {
            return -1L;
        }

        long seconds = next.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(seconds, 0L);
    }

    public synchronized void refresh(JobKey key) {
        states.remove(key);
    }

    private GateState ensureState(JobKey key) {
        String cron = schedulerControlService.getCron(key);
        GateState current = states.get(key);

        if (current == null || !current.cron().equals(cron)) {
            GateState refreshed = computeNextState(key, cron, ZonedDateTime.now());
            states.put(key, refreshed);
            return refreshed;
        }

        return current;
    }

    private GateState computeNextState(JobKey key, String cron, ZonedDateTime from) {
        CronExpression expression = CronExpression.parse(cron);
        ZonedDateTime nextRun = expression.next(from);

        return new GateState(
                cron,
                nextRun == null ? null : nextRun.withZoneSameInstant(ZoneId.systemDefault()).toInstant()
        );
    }

    private record GateState(String cron, Instant nextRun) {
    }
}

