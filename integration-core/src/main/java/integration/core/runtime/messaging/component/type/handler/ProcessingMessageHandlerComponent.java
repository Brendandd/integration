package integration.core.runtime.messaging.component.type.handler;


import integration.core.domain.IdentifierType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.exception.retryable.QueuePublishingException;

/**
 *  Base class for message handlers which do processing.  Most should but filters components do not.
 * 
 * 
 * @author Brendan Douglas
 *
 */
public abstract class ProcessingMessageHandlerComponent extends BaseMessageHandlerComponent {

    
    @Override
    public void configure() throws Exception {
        super.configure();
        
        configureProcessingQueueProducer();
        configureProcessingQueueConsumer();
    }
    
    /**
     * The default routing.  Subclasses can override if required.
     */
    @Override
    protected void configureEventRouting() {
        eventRoutingMap.put(OutboxEventType.INGRESS_COMPLETE, "toProcessingQueue");
        eventRoutingMap.put(OutboxEventType.PROCESSING_COMPLETE, "toEgressQueue");
        eventRoutingMap.put(OutboxEventType.PENDING_FORWARDING, "egressForwarding");
    }
    
    

    public abstract void configureProcessingQueueConsumer() throws ComponentConfigurationException, RouteConfigurationException;
    
    
    
    
    public void configureProcessingQueueProducer() throws ComponentConfigurationException, RouteConfigurationException {
        // A route to place the ingress message flow onto the processing queue
        from("direct:toProcessingQueue-" + getIdentifier())
            .routeId("toProcessingQueue-" + getIdentifier())
            .routeGroup(getComponentPath())  
            .transacted()
                .process(exchange -> {                       
                    Long eventId = null;
                    Long messageFlowId = null;
                    
                    // Delete the event.
                    eventId = exchange.getMessage().getBody(Long.class);
                    exchange.getMessage().setHeader(IdentifierType.OUTBOX_EVENT_ID.name(), eventId);
                    
                    outboxService.deleteEvent(eventId);
                    
                    // Get the message flow step id.
                    messageFlowId = (Long)exchange.getMessage().getHeader(IdentifierType.MESSAGE_FLOW_ID.name());

                    // Add the message to the queue
                    try {
                        producerTemplate.sendBody("jms:queue:processingQueue-" + getIdentifier(), messageFlowId);
                    } catch(Exception e) {
                        throw new QueuePublishingException("Error adding the message flow id to the processing queue", eventId, getIdentifier(), messageFlowId, e);
                    }
                });
    }
}