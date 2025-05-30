package integration.core.runtime.messaging.component;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.exception.retryable.MessageForwardingException;
import integration.core.runtime.messaging.service.OutboxService;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IntraRouteJMSTopicProducerEgressForwardingProcessor extends BaseMessageFlowProcessor<MessageProducer> {
    
    @Autowired
    private OutboxService outboxService;
    
    @Autowired
    private ProducerTemplate producerTemplate;

    @Override
    public void process(Exchange exchange) throws Exception {              
        // Delete the event.
        long eventId = (long)exchange.getMessage().getHeader(IdentifierType.OUTBOX_EVENT_ID.name());
        outboxService.deleteEvent(eventId);
    
        long messageFlowId = (Long)exchange.getMessage().getHeader(IdentifierType.MESSAGE_FLOW_ID.name());
        MessageFlowDto messageFlowDto = messageFlowService.retrieveMessageFlow(messageFlowId);
        
        // Change the status of the message flow from pending forwarding to forwarded
        messageFlowService.updateAction(messageFlowId, MessageFlowActionType.FORWARDED);
           
        try {
            producerTemplate.sendBody("jms:topic:VirtualTopic." + component.getComponentPath(), messageFlowDto.getId());
        } catch(Exception e) {
            throw new MessageForwardingException("Error forwarding message out of component", eventId, component.getIdentifier(), messageFlowDto.getId(), e);
        }
    }
}
