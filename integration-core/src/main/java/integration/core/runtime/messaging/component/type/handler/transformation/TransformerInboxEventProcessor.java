package integration.core.runtime.messaging.component.type.handler.transformation;

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
 * Inbox event processor for a all message transformers
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TransformerInboxEventProcessor extends BaseMessageFlowProcessor<BaseTransformationComponent> implements InboxEventProcessor {
    
    @Autowired
    protected OutboxService outboxService;
    
    @Autowired
    protected InboxService inboxService;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        MessageFlowDto messageFlowDto = null;
        Long messageFlowId = null;
        
        try {
            messageFlowId = exchange.getMessage().getBody(Long.class);
            exchange.getMessage().setHeader(IdentifierType.MESSAGE_FLOW_ID.name(), messageFlowId);
            messageFlowDto = messageFlowService.retrieveMessageFlow(messageFlowId, true);
                    
            // Apply acceptance policy.
            MessageFlowPolicyResult acceptancePolicyResult = component.getMessageAcceptancePolicy().applyPolicy(messageFlowDto);
            if (acceptancePolicyResult.isSuccess()) {
                messageFlowId = messageFlowService.recordMessageAccepted(component.getIdentifier(), messageFlowId);
                
                // Message has been accepted so transform the message content.
                String transformedContent = component.getTransformer().transform(messageFlowDto);
                messageFlowId = messageFlowService.recordNewContentMessageFlow(transformedContent, component.getIdentifier(),messageFlowId, component.getContentType(), MessageFlowActionType.TRANSFORMED);               
                                  
                // Now apply the forwarding policy against the transformed message
                MessageFlowPolicyResult forwardingPolicyResult = component.getMessageForwardingPolicy().applyPolicy(messageFlowDto);
                if (forwardingPolicyResult.isSuccess()) {
                    messageFlowId = messageFlowService.recordMessagePendingForwarding(component.getIdentifier(), messageFlowId);
                    outboxService.recordEvent(messageFlowId,component.getIdentifier(), component.getRoute().getIdentifier(), component.getOwner());
                } else {
                    messageFlowService.recordMessageNotForwarded(component.getIdentifier(), messageFlowId, forwardingPolicyResult);
                }  
            } else {
                messageFlowService.recordMessageNotAccepted(component.getIdentifier(), messageFlowId, acceptancePolicyResult);
            } 
            
            // Now delete the event from the inbox.
            Long eventId = (Long)exchange.getMessage().getHeader(IdentifierType.OUTBOX_EVENT_ID.name()); 
            inboxService.deleteEvent(eventId);
            
        } catch(Exception e) {
            throw new InboxEventSchedulerException(component.getIdentifier(), messageFlowId, e);
        }          
    }
}
