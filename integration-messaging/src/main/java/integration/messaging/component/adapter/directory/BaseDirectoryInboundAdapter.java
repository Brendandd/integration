package integration.messaging.component.adapter.directory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.processor.idempotent.jpa.JpaMessageIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.messaging.component.adapter.BaseInboundAdapter;
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

    public BaseDirectoryInboundAdapter(String componentName) {
        super(componentName);
    }

    @Autowired
    private EntityManagerFactory emf;

    public String getSourceFolder() {
        return componentProperties.get("SOURCE_FOLDER");
    }

    @Override
    public String getFromUriString() {
        return "file:" + getSourceFolder() + "?idempotent=true&idempotentRepository=#jpaStore" + getOptions();
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
            .routeId("mllpInboundMessageHandlerRoute-" + identifier.getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(identifier.getComponentPath())
            .autoStartup(isInboundRunning)
            .transacted()
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Store the message received by this inbound adapter.
                        String messageContent = exchange.getMessage().getBody(String.class);
                        long messageFlowStepId = messagingFlowService.recordMessageReceivedFromExternalSource(messageContent, BaseDirectoryInboundAdapter.this, getContentType());
                        
                        messagingFlowService.recordMessageFlowEvent(messageFlowStepId, MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }
}