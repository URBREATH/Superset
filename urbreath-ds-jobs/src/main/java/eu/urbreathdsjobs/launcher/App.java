package eu.urbreathdsjobs.launcher;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication(scanBasePackages = {
		"eu.urbreathdsjobs.launcher", 
		"eu.urbreathdsjobs.config", 
		"eu.urbreathdsjobs.reader", 
		"eu.urbreathdsjobs.processor",
		"eu.urbreathdsjobs.listener",
		"eu.urbreathdsjobs.dto",
		"eu.urbreathdsjobs.client.minio",
		"eu.urbreathdsjobs.writer"})
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}