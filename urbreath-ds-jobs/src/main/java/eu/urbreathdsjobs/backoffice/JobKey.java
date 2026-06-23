package eu.urbreathdsjobs.backoffice;

import java.util.Arrays;
import java.util.Optional;

public enum JobKey {
    TRAFFIC_IMPORT("traffic", "Traffic Import", "trafficJob", 1L,
            "app.scheduler.import.traffic.enabled",
            "app.scheduler.import.traffic.cron",
            "0/30 * * * * *"),

    TEMPERATURE_IMPORT("temperature", "Temperature Import", "temperatureImportJob", 2L,
            "app.scheduler.import.temperature.enabled",
            "app.scheduler.import.temperature.cron",
            "0/30 * * * * *"),

    PRECIPITATION_IMPORT("precipitation", "Precipitation Import", "precipitationImportJob", 3L,
            "app.scheduler.import.precipitation.enabled",
            "app.scheduler.import.precipitation.cron",
            "0/30 * * * * *"),

    WMS_IMPORT("wms", "WMS Import", "wmsImportJob", 4L,
            "app.scheduler.wms.enabled",
            "app.scheduler.wms.cron",
            "0 0/5 * * * *"),

    FROST_IMPORT("frost-import", "Frost Import", "frostImportJob", 5L,
            "app.scheduler.frost.import.enabled",
            "app.scheduler.frost.import.cron",
            "0 0/20 * * * *"),

    FROST_CONFIG("frost-config", "Frost Config", "frostConfigJob", 6L,
            "app.scheduler.frost.config.enabled",
            "app.scheduler.frost.config.cron",
            "0 0/20 * * * *");

    private final String id;
    private final String displayName;
    private final String jobName;
    private final Long batchId;
    private final String enabledProperty;
    private final String cronProperty;
    private final String defaultCron;

    JobKey(
            String id,
            String displayName,
            String jobName,
            Long batchId,
            String enabledProperty,
            String cronProperty,
            String defaultCron
    ) {
        this.id = id;
        this.displayName = displayName;
        this.jobName = jobName;
        this.batchId = batchId;
        this.enabledProperty = enabledProperty;
        this.cronProperty = cronProperty;
        this.defaultCron = defaultCron;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getJobName() {
        return jobName;
    }

    public Long getBatchId() {
        return batchId;
    }

    public String getEnabledProperty() {
        return enabledProperty;
    }

    public String getCronProperty() {
        return cronProperty;
    }

    public String getDefaultCron() {
        return defaultCron;
    }

    public static Optional<JobKey> fromPath(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(k -> k.id.equalsIgnoreCase(value) || k.name().equalsIgnoreCase(value))
                .findFirst();
    }
}

