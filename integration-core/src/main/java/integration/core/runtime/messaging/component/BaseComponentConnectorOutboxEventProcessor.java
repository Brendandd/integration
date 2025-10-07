package integration.core.runtime.messaging.component;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.IdentifierType;
import integration.core.runtime.messaging.exception.retryable.JMSForwardingException;
import integration.core.runtime.messaging.exception.retryable.OutboxEventSchedulerException;
import integration.core.runtime.messaging.service.OutboxService;

/**
 * A processor to write a message flow id to JMS so it can be consumed by other components.
 */
public abstract class BaseComponentConnectorOutboxEventProcessor<T extends MessagingComponent> extends BaseMessageFlowProcessor<MessagingComponent>implements OutboxEventProcessor {
    
    @Autowired
    private ProducerTemplate producerTemplate;
    
    @Autowired
    private OutboxService outboxService;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        
        Long messageFlowId = null;
        
        try {
            messageFlowId = (Long)exchange.getMessage().getHeader(IdentifierType.MESSAGE_FLOW_ID.name());
            Long eventId = (Long)exchange.getMessage().getHeader(IdentifierType.EVENT_ID.name());
            
            outboxService.deleteEvent(eventId);
            
            messageFlowService.updatePendingForwardingToForwardedAction(messageFlowId);
            
            // Write the message flow if to the topic.
            try {               
                producerTemplate.sendBody("jms:topic:VirtualTopic." + component.getComponentPath(), messageFlowId);
            } catch(Exception e) {
                throw new JMSForwardingException(eventId, component.getIdentifier(), messageFlowId, e);
            }
        } catch(Exception e) {
            throw new OutboxEventSchedulerException(component.getIdentifier(), messageFlowId, e);
        }  
    }
}
