package eu.urbreathdsjobs.backoffice;

import org.springframework.core.env.Environment;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class SchedulerControlService {

    private final Environment environment;
    private final Map<JobKey, RuntimeSchedule> runtimeSchedules = new EnumMap<>(JobKey.class);

    public SchedulerControlService(Environment environment) {
        this.environment = environment;
        for (JobKey key : JobKey.values()) {
            runtimeSchedules.put(key, new RuntimeSchedule(readEnabled(key), readCron(key)));
        }
    }

    public synchronized RuntimeSchedule update(JobKey key, Boolean enabled, String cron) {
        RuntimeSchedule current = runtimeSchedules.get(key);
        boolean targetEnabled = enabled != null ? enabled : current.enabled();
        String targetCron = cron != null && !cron.isBlank() ? cron.trim() : current.cron();
        CronExpression.parse(targetCron);

        RuntimeSchedule updated = new RuntimeSchedule(targetEnabled, targetCron);
        runtimeSchedules.put(key, updated);
        return updated;
    }

    public synchronized RuntimeSchedule get(JobKey key) {
        return runtimeSchedules.get(key);
    }

    public synchronized Map<JobKey, RuntimeSchedule> snapshot() {
        return new EnumMap<>(runtimeSchedules);
    }

    public synchronized boolean isEnabled(JobKey key) {
        RuntimeSchedule state = runtimeSchedules.get(key);
        return state != null && state.enabled();
    }

    public synchronized String getCron(JobKey key) {
        RuntimeSchedule state = runtimeSchedules.get(key);
        return state != null ? state.cron() : key.getDefaultCron();
    }

    private boolean readEnabled(JobKey key) {
        return environment.getProperty(key.getEnabledProperty(), Boolean.class, false);
    }

    private String readCron(JobKey key) {
        return environment.getProperty(key.getCronProperty(), key.getDefaultCron());
    }

    public record RuntimeSchedule(boolean enabled, String cron) {
    }
}

