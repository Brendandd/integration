package integration.core.runtime.messaging.component.type.adapter.outbound;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.IdentifierType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.component.InboxEventProcessor;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.exception.retryable.InboxEventSchedulerException;
import integration.core.runtime.messaging.service.InboxService;
import integration.core.runtime.messaging.service.OutboxService;


/**
 * Inbox event processor for a all outbound adapter.
 */
public abstract class BaseOutboundAdapterInboxEventProcessor extends BaseMessageFlowProcessor<BaseOutboundAdapter> implements InboxEventProcessor {
        
    @Autowired
    protected OutboxService outboxService;
    
    @Autowired
    protected InboxService inboxService;
    
    
    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange, true);
                    
            // Apply acceptance policy.
            MessageFlowPolicyResult result = component.getMessageAcceptancePolicy().applyPolicy(parentMessageFlowDto);
            if (result.isSuccess()) {
                MessageFlowDto acceptedMessageFlowDto = messageFlowService.recordMessageAccepted(component.getIdentifier(), parentMessageFlowDto.getId());
                MessageFlowDto pendingForwardingMessageFlowDto = messageFlowService.recordMessagePendingForwarding(component.getIdentifier(), acceptedMessageFlowDto.getId());
                                  
                // Record an event so the message can be forwarded to other components for processing.
                outboxService.recordEvent(pendingForwardingMessageFlowDto.getId(),component.getIdentifier(), component.getRoute().getIdentifier(), component.getOwner()); 
            } else {
                messageFlowService.recordMessageNotAccepted(component.getIdentifier(), parentMessageFlowDto.getId(), result);
            } 
            
            // Now delete the event from the inbox.
            Long eventId = (Long)exchange.getMessage().getHeader(IdentifierType.OUTBOX_EVENT_ID.name()); 
            inboxService.deleteEvent(eventId);
            
        } catch(Exception e) {
            throw new InboxEventSchedulerException(component.getIdentifier(), e);
        }       
    }
}
