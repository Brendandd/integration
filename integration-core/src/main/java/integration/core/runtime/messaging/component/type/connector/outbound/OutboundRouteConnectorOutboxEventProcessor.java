package integration.core.runtime.messaging.component.type.connector.outbound;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.component.OutboxEventProcessor;
import integration.core.runtime.messaging.exception.retryable.JMSForwardingException;
import integration.core.runtime.messaging.exception.retryable.OutboxEventSchedulerException;
import integration.core.runtime.messaging.service.OutboxService;


/**
 * Outbox event processor for outbound route connectors.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OutboundRouteConnectorOutboxEventProcessor extends BaseMessageFlowProcessor<BaseOutboundRouteConnectorComponent> implements OutboxEventProcessor {
    
    @Autowired
    protected OutboxService outboxService;
    
    @Autowired
    protected ProducerTemplate producerTemplate;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        Long messageFlowId = null;
        
        try {
            
            // Delete the event.
            long eventId = (long)exchange.getMessage().getHeader(IdentifierType.EVENT_ID.name());
            outboxService.deleteEvent(eventId);
        
            messageFlowId = (Long)exchange.getMessage().getHeader(IdentifierType.MESSAGE_FLOW_ID.name());
            
            messageFlowService.updatePendingForwardingToForwardedAction(messageFlowId);
            
            // Write the message flow if to the topic.
            try {
                producerTemplate.sendBody("jms:topic:VirtualTopic." + component.getConnectorName(exchange), messageFlowId);
            } catch(Exception e) {
                throw new JMSForwardingException(eventId, component.getIdentifier(), messageFlowId, e);
            }
        } catch(Exception e) {
            throw new OutboxEventSchedulerException(component.getIdentifier(), messageFlowId, e);
        }       
      
    }
}
