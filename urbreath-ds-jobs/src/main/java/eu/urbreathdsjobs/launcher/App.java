package eu.urbreathdsjobs.launcher;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication(scanBasePackages = "eu.urbreathdsjobs")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}