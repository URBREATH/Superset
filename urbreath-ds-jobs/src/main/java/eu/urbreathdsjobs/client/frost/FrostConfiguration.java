package eu.urbreathdsjobs.client.frost;

import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class FrostConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "frost", name = "enabled", havingValue = "true")
    public SensorThingsService frostSensorThingsService(FrostProperties frostProperties) throws Exception {
        return new SensorThingsService(URI.create(frostProperties.getBaseUrl()));
    }
}

