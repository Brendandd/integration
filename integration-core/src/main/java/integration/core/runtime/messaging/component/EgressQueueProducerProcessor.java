package integration.core.runtime.messaging.component;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.runtime.messaging.exception.retryable.QueuePublishingException;
import integration.core.runtime.messaging.service.OutboxService;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EgressQueueProducerProcessor extends BaseMessageFlowProcessor<MessagingComponent> {

    @Autowired
    private ProducerTemplate producerTemplate;
    
    @Autowired
    private OutboxService outboxService;

    
    @Override
    public void process(Exchange exchange) throws Exception {
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
            producerTemplate.sendBody("jms:queue:egressQueue-" + component.getIdentifier(), messageFlowId);
        } catch(Exception e) {
            throw new QueuePublishingException("Error adding the message flow id to the egress queue", eventId, component.getIdentifier(), messageFlowId, e);
        }
    }
}
