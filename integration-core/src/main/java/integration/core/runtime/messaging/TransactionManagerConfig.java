package integration.core.runtime.messaging;

import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;

import jakarta.jms.ConnectionFactory;
import jakarta.persistence.EntityManagerFactory;

/**
 * Apache Camel config.
 * 
 * @author Brendan Douglas
 */
@Configuration
public class TransactionManagerConfig {

    @Bean(name = {"jpaTransactionManager", "transactionManager"})
    @Primary
    public JpaTransactionManager jpaTransactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public JmsTransactionManager jmsTransactionManager(ConnectionFactory cf) {
        return new JmsTransactionManager(cf);
    }

    
    @Bean("jpaTransactionPolicy")
    public SpringTransactionPolicy jpaTransactionPolicy(JpaTransactionManager jpaTransactionManager) {
        SpringTransactionPolicy policy = new SpringTransactionPolicy();
        policy.setTransactionManager(jpaTransactionManager);
        policy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return policy;
    }

    @Bean("jmsTransactionPolicy")
    public SpringTransactionPolicy jmsTransactionPolicy(JmsTransactionManager jmsTransactionManager) {
        SpringTransactionPolicy policy = new SpringTransactionPolicy();
        policy.setTransactionManager(jmsTransactionManager);
        policy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return policy;
    }
}