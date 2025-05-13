package integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "integration.core")
@EntityScan(basePackages = "integration.core")
@ComponentScan(basePackages = {"integration"})
@EnableAsync
@Configuration
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
