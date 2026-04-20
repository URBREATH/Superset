package eu.urbreathdsjobs.client.wms;

/**
 * Enum representing the cities for WMS requests
 */
public enum City {
    MADRID("MADRID"),
    LEUVEN("LEUVEN"),
    CLUJ("CLUJ"),
    TALLIN("TALLIN");

    private final String name;

    City(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

