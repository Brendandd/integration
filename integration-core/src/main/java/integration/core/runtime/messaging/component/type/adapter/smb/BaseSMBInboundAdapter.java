package integration.core.runtime.messaging.component.type.adapter.smb;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.idempotent.jpa.JpaMessageIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.adapter.BaseInboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.annotation.StoreHeader;
import jakarta.persistence.EntityManagerFactory;

/**
 * Base class for all SMB inbound adapters. This components
 * reads the file, stores it and writes and event No other processing should be
 * done here.
 * 
 * @author Brendan Douglas
 *
 */
@StoreHeader(name = "CamelFileName")
@ComponentType(type = IntegrationComponentTypeEnum.INBOUND_SMB_ADAPTER)
public abstract class BaseSMBInboundAdapter extends BaseInboundAdapter {
    
    @Autowired
    private EntityManagerFactory emf;

    public String getSourceFolder() {
        return componentProperties.get("SOURCE_FOLDER");
    }
    
    public String getHost() {
        return componentProperties.get("HOST");
    }

    
    @Override
    public String getFromUriString() {
        return "smb:" + getHost() + "/" + getSourceFolder() + constructOptions();
    }

    
    @Bean
    protected JpaMessageIdRepository jpaStore() {
        return new JpaMessageIdRepository(emf, "FileRepo");
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();

        // A route to read a file from the defined location, store the file content, store an event all within a single transaction.
        from(getFromUriString())
            .routeId("inboundEntryPoint-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
            .transacted()
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                       
                        // Store the message received by this inbound adapter.
                        String messageContent = exchange.getMessage().getBody(String.class);
                        MessageFlowDto messageFlowDto = messagingFlowService.recordMessageFlow(messageContent, getIdentifier(), getContentType(), MessageFlowActionType.ACCEPTED);
                        
                        
                        addProperties(exchange, messageFlowDto.getId());
                        
                        // Final step in the inbound message handling is to write an event which will put the message onto a queue for this components outbound message handler to pick up and process.
                        messagingFlowService.recordMessageFlowEvent(messageFlowDto.getId(),getIdentifier(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }
}