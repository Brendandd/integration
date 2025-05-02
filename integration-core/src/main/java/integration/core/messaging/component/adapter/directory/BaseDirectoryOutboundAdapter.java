package integration.core.messaging.component.adapter.directory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentType;
import integration.core.dto.MessageFlowStepDto;
import integration.core.messaging.component.adapter.BaseOutboundAdapter;

/**
 * Base class for all directory output communication points.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseDirectoryOutboundAdapter extends BaseOutboundAdapter {
    private static final String CAMEL_FILE_NAME = "CamelFileName";

    public String getDestinationFolder() {
        return componentProperties.get("TARGET_FOLDER");
    }
    
    @Override
    public String getToUriString() {
        return "file:" + getDestinationFolder() + constructOptions();
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.OUTBOUND_DIRECTORY_ADAPTER;
    }

    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.OUTBOUND_ADAPTER;
    }
    
    
    /**
     * Default implementation which returns the original filename.
     * 
     * @param exchange
     * @param messageFlowId
     * @return
     */
    protected String generateFilename(Exchange exchange, long messageFlowId) {
        String originalFileName = messagingFlowService.retrieveMessageMetaData(CAMEL_FILE_NAME, messageFlowId);
        
        return originalFileName;      
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();
        
        // A route to process outbound message handling complete events.  This is the final stage of an inbound adapter.
        from("direct:handleOutboundMessageHandlingCompleteEvent-" + getComponentPath())
            .routeId("handleOutboundMessageHandlingCompleteEvent-" + getComponentPath())
            .routeGroup(getComponentPath())
            .transacted()
                .process(new Processor() {
    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Delete the event.
                        Long eventId = exchange.getMessage().getBody(Long.class);
                        messagingFlowService.deleteEvent(eventId);
                        
                        // Get the message content
                        Long messageFlowId = (Long)exchange.getMessage().getHeader(MESSAGE_FLOW_STEP_ID);
                        MessageFlowStepDto messageFlowStepDto = messagingFlowService.retrieveMessageFlow(messageFlowId);
                        
                        String fileName = generateFilename(exchange, messageFlowId);
                       
                        exchange.getMessage().setHeader(CAMEL_FILE_NAME, fileName);

                        exchange.getMessage().setBody(messageFlowStepDto.getMessageContent());   
                    }
                })
                .to(getToUriString());
    }
}
