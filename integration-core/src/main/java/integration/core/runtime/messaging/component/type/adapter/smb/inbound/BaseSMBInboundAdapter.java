package integration.core.runtime.messaging.component.type.adapter.smb.inbound;

import java.util.Map;

import org.apache.camel.processor.idempotent.jpa.JpaMessageIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.inbound.BaseInboundAdapter;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

/**
 * Base class for all SMB inbound adapters. This components
 * reads the file, stores it and writes and event No other processing should be
 * done here.
 * 
 * @author Brendan Douglas
 *
 */
@ComponentType(type = IntegrationComponentTypeEnum.INBOUND_SMB_ADAPTER)
public abstract class BaseSMBInboundAdapter extends BaseInboundAdapter {
    
    @Autowired
    private EntityManagerFactory emf;
    
    @Autowired
    private SMBInboundAdapterInboxEventProcessor inboxEventProcessor;
    
    @Autowired
    private SMBInboundAdapterOutboxEventProcessor outboxEventProcessor;
    
    @PostConstruct
    public void BaseSMBInboundAdapterInit() {
        inboxEventProcessor.setComponent(this);
        outboxEventProcessor.setComponent(this);
    }
    

    public String getSourceFolder() {
        return componentProperties.get("SOURCE_FOLDER");
    }
    
    public String getHost() {
        return componentProperties.get("HOST");
    }

    
    @Override
    public String getFromUriString() {
        return "smb:" + getHost() + "/" + getSourceFolder() + constructAdapterOptions();
    }

    
    @Bean
    protected JpaMessageIdRepository jpaStore() {
        return new JpaMessageIdRepository(emf, "FileRepo");
    }
    
    
    @Override
    public SMBInboundAdapterInboxEventProcessor getInboxEventProcessor() {
        return inboxEventProcessor;
    }

    
    @Override
    public SMBInboundAdapterOutboxEventProcessor getOutboxEventProcessor() {
        return outboxEventProcessor;
    }

    
    @Override
    public void configureIngressRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        // A route to read a file from the defined location, store the file content, store an event all within a single transaction.
        from(getFromUriString())
            .routeId("ingress-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
            .transacted("jpaTransactionPolicy")
            
                .process(exchange -> {
                    Map<String, Object> headers = exchange.getMessage().getHeaders();
                    
                    // Store the message received by this inbound adapter.
                    String messageContent = exchange.getMessage().getBody(String.class);
                    Long messageFlowId = messageFlowService.recordInitialMessageFlow(messageContent, getIdentifier(), getContentType(), headers, MessageFlowActionType.INGESTED);                                       
                    inboxService.recordEvent(messageFlowId,getIdentifier(), getRoute().getIdentifier(), getOwner()); 
                });
        
    }
}