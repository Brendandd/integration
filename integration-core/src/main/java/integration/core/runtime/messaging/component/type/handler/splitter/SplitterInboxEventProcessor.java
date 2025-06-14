package integration.core.runtime.messaging.component.type.handler.splitter;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.component.InboxEventProcessor;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.exception.retryable.InboxEventSchedulerException;
import integration.core.runtime.messaging.service.InboxService;
import integration.core.runtime.messaging.service.OutboxService;


/**
 * Inbox event processor for a all message splitters.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SplitterInboxEventProcessor extends BaseMessageFlowProcessor<BaseSplitterComponent> implements InboxEventProcessor {
    
    @Autowired
    protected OutboxService outboxService;
    
    @Autowired
    protected InboxService inboxService;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange, true);
                    
            // Apply acceptance policy.
            MessageFlowPolicyResult acceptancePolicyResult = component.getMessageAcceptancePolicy().applyPolicy(parentMessageFlowDto);
            if (acceptancePolicyResult.isSuccess()) {
                MessageFlowDto acceptedMessageFlowDto = messageFlowService.recordMessageAccepted(component.getIdentifier(), parentMessageFlowDto.getId());
                
                // Message has been accepted so apply the splitter rules.
                int numberOfMessages = component.getSplitter().getSplitCount(parentMessageFlowDto);
                
                for (int i = 0; i < numberOfMessages; i++) {
                    MessageFlowDto splitMessageFlowDto = messageFlowService.recordMessageFlowWithSameContent(component.getIdentifier(), acceptedMessageFlowDto.getId(), MessageFlowActionType.CREATED_FROM_SPLIT);               
                                  
                    // Now apply the forwarding policy against the split message
                    MessageFlowPolicyResult forwardingPolicyResult = component.getMessageForwardingPolicy().applyPolicy(splitMessageFlowDto);
               
                    if (forwardingPolicyResult.isSuccess()) {
                        MessageFlowDto pendingForwardingMessageFlowDto = messageFlowService.recordMessagePendingForwarding(component.getIdentifier(), splitMessageFlowDto.getId());
                        outboxService.recordEvent(pendingForwardingMessageFlowDto.getId(),component.getIdentifier(), component.getRoute().getIdentifier(), component.getOwner());
                    } else {
                        messageFlowService.recordMessageNotForwarded(component.getIdentifier(), splitMessageFlowDto.getId(), forwardingPolicyResult);
                    }
                }
            } else {
                messageFlowService.recordMessageNotAccepted(component.getIdentifier(), parentMessageFlowDto.getId(), acceptancePolicyResult);
            } 
            
            // Now delete the event from the inbox.
            Long eventId = (Long)exchange.getMessage().getHeader(IdentifierType.OUTBOX_EVENT_ID.name()); 
            inboxService.deleteEvent(eventId);
            
        } catch(Exception e) {
            throw new InboxEventSchedulerException(component.getIdentifier(), e);
        }           
    }
}
