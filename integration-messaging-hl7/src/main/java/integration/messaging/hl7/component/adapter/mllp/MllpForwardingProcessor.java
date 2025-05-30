package integration.messaging.hl7.component.adapter.mllp;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.exception.retryable.MessageForwardingException;
import integration.core.runtime.messaging.service.OutboxService;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MllpForwardingProcessor extends BaseMessageFlowProcessor<BaseMllpOutboundAdapter> {
    
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
           
        String target = component.getTargetHost() + ":" + component.getTargetPort();
        String uri = "netty:tcp://" + target + component.constructAdapterOptions();
        
        try {
            producerTemplate.sendBodyAndHeaders(uri, messageFlowDto.getMessageContent(), component.getHeaders(messageFlowDto));
        } catch(Exception e) {
            throw new MessageForwardingException("Error sending message via MLLP", eventId, component.getIdentifier(), messageFlowDto.getId(), e);
        }
    }
}
