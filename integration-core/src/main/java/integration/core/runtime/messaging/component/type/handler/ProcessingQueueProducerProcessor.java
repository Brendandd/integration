package integration.core.runtime.messaging.component.type.handler;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.exception.retryable.QueuePublishingException;
import integration.core.runtime.messaging.service.OutboxService;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProcessingQueueProducerProcessor extends BaseMessageFlowProcessor<ProcessingMessageHandlerComponent>{
    @Autowired
    private OutboxService outboxService;
    
    @Autowired
    private ProducerTemplate producerTemplate;
    

    @Override
    public void process(Exchange exchange) throws Exception {
       
        // Delete the event.
        Long eventId = exchange.getMessage().getBody(Long.class);
        exchange.getMessage().setHeader(IdentifierType.OUTBOX_EVENT_ID.name(), eventId);
        
        outboxService.deleteEvent(eventId);
        
        // Get the message flow step id.
        Long messageFlowId = (Long)exchange.getMessage().getHeader(IdentifierType.MESSAGE_FLOW_ID.name());

        // Add the message to the queue
        try {
            producerTemplate.sendBody("jms:queue:processingQueue-" + component.getIdentifier(), messageFlowId);
        } catch(Exception e) {
            throw new QueuePublishingException("Error adding the message flow id to the processing queue", eventId, component.getIdentifier(), messageFlowId, e);
        }
    } 
}
