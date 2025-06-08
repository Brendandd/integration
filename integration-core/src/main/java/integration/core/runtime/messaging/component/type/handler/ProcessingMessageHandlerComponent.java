package integration.core.runtime.messaging.component.type.handler;


import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.messaging.OutboxEventType;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import jakarta.annotation.PostConstruct;

/**
 *  Base class for message handlers which do processing.  Most should but filters components do not.
 * 
 * 
 * @author Brendan Douglas
 *
 */
public abstract class ProcessingMessageHandlerComponent extends BaseMessageHandlerComponent {

    @Autowired
    private ProcessingQueueProducerProcessor processingQueueProducerProcessor;
        
    @PostConstruct
    public void ProcessingMessageHandlerComponentInit() {
        processingQueueProducerProcessor.setComponent(this);
    }
    
    
    @Override
    public void configure() throws Exception {
        super.configure();
        
        // A route to add a message flow id to the processing queue
        from("direct:toProcessingQueue-" + getIdentifier())
            .routeId("toProcessingQueue-" + getIdentifier())
            .routeGroup(getComponentPath())  
            .transacted()
                .process(processingQueueProducerProcessor);        

        
        // A route to consume a message flow id from the processing route.
        from("jms:queue:processingQueue-" + getIdentifier() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("startProcessing-" + getIdentifier())
            .routeGroup(getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .transacted()   
                .process(getProcessingProcessor());
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

    
    /**
     * A Camel processor to do the message processing.
     * 
     * @return
     */
    public abstract BaseMessageFlowProcessor<?> getProcessingProcessor();
}