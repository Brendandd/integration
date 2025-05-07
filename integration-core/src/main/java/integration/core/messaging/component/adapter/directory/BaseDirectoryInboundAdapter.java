package integration.core.messaging.component.adapter.directory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.idempotent.jpa.JpaMessageIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentState;
import integration.core.domain.configuration.ComponentType;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.messaging.component.adapter.BaseInboundAdapter;
import jakarta.persistence.EntityManagerFactory;

/**
 * Base class for all directory/file input adapters. This components
 * reads the file, stores it and writes and event No other processing should be
 * done here.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseDirectoryInboundAdapter extends BaseInboundAdapter {
    private static final String CAMEL_FILE_NAME = "CamelFileName";
    
    @Autowired
    private EntityManagerFactory emf;

    public String getSourceFolder() {
        return componentProperties.get("SOURCE_FOLDER");
    }
    
    
    @Override
    public String getFromUriString() {
        return "file:" + getSourceFolder() + constructOptions();
    }

    
    @Bean
    protected JpaMessageIdRepository jpaStore() {
        return new JpaMessageIdRepository(emf, "FileRepo");
    }

    @Override
    public ComponentType getType() {
        return ComponentType.INBOUND_DIRECTORY_ADAPTER;
    }

    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.INBOUND_ADAPTER;
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();

        // A route to read a file from the defined location, store the file content, store an event all within a single transaction.
        from(getFromUriString())
            .routeId("inboundEntryPoint-" + getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .autoStartup(inboundState == ComponentState.RUNNING)
            .transacted()
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                       
                        // Store the message received by this inbound adapter.
                        String messageContent = exchange.getMessage().getBody(String.class);
                        MessageFlowDto messageFlowDto = messagingFlowService.recordMessageFlow(messageContent, BaseDirectoryInboundAdapter.this, getContentType(), MessageFlowActionType.ACCEPTED);
                        
                        
                        addProperties(exchange, messageFlowDto.getId());
                        
                        // Final step in the inbound message handling is to write an event which will put the message onto a queue for this components outbound message handler to pick up and process.
                        messagingFlowService.recordMessageFlowEvent(messageFlowDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }

    
    private void addProperties(Exchange exchange, long messageFlowId) {
        // Store the incoming file name header.
        String incomingFilename = (String)exchange.getMessage().getHeader(CAMEL_FILE_NAME);
        messageFlowPropertyService.addProperty(CAMEL_FILE_NAME, incomingFilename, messageFlowId);
    }
}