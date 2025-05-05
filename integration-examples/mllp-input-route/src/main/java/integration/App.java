package integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = { "integration.core", "integration.messaging.core" })
@EnableJpaRepositories(basePackages = "integration.core.repository")
@EntityScan(basePackages = "integration.core.domain")
@ComponentScan(basePackages = { "integration.core", "integration.route", "integration.component", "integration.messaging","integration.messaging.hl7","integration.core.messaging" })
@EnableAsync
@Configuration
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
