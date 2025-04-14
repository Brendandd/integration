package integration.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = { "integration.core", "integration.messaging.core" })
@EnableJpaRepositories(basePackages = "integration.core.repository")
@EntityScan(basePackages = "integration.core.domain")
@ComponentScan(basePackages = { "integration.core", "integration.messaging", "integration.rest" })
@EnableIntegration
@EnableAsync
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
