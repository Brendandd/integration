package integration.messaging.component.adapter.directory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.messaging.component.BaseMessagingComponent;
import integration.messaging.component.adapter.BaseOutboundAdapter;

/**
 * Base class for all directory output communication points.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseDirectoryOutboundAdapter extends BaseOutboundAdapter {
    private static final String CAMEL_FILE_NAME = "CamelFileName";

    public BaseDirectoryOutboundAdapter(String componentName) {
        super(componentName);
    }

    public String getDestinationFolder() {
        return componentProperties.get("TARGET_FOLDER");
    }
    
    @Override
    public String getToUriString() {
        return "file:" + getDestinationFolder() + constructOptions();
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
        from("direct:handleOutboundMessageHandlingCompleteEvent-" + identifier.getComponentPath())
            .routeId("handleOutboundMessageHandlingCompleteEvent-" + identifier.getComponentPath())
            .routeGroup(identifier.getComponentPath())
            .transacted()
                .process(new Processor() {
    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Delete the event.
                        Long eventId = exchange.getMessage().getBody(Long.class);
                        messagingFlowService.deleteEvent(eventId);
                        
                        // Get the message content
                        Long messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_STEP_ID);
                        String messageContent = messagingFlowService.retrieveMessageContent(messageFlowId);
                        
                        String fileName = generateFilename(exchange, messageFlowId);
                        getLogger().info("********BRENDAN *****: " + fileName);
                        
                        exchange.getMessage().setHeader(CAMEL_FILE_NAME, fileName);

                        exchange.getMessage().setBody(messageContent);   
                    }
                })
                .to(getToUriString());
    }
}
