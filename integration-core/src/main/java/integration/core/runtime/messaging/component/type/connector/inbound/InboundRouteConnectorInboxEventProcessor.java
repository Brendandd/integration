package integration.core.runtime.messaging.component.type.connector.inbound;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.component.InboxEventProcessor;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.exception.retryable.InboxEventSchedulerException;
import integration.core.runtime.messaging.service.InboxService;
import integration.core.runtime.messaging.service.OutboxService;


/**
 * Inbox event processor for inbound route connectors.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InboundRouteConnectorInboxEventProcessor extends BaseMessageFlowProcessor<BaseInboundRouteConnectorComponent> implements InboxEventProcessor {

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
            
            Long eventId = (Long)exchange.getMessage().getHeader(IdentifierType.EVENT_ID.name());
    
            // Apply the message forwarding rules
            MessageFlowPolicyResult result = component.getMessageForwardingPolicy().applyPolicy(messageFlowDto);
                                                           
            if (result.isSuccess()) {
                messageFlowId = messageFlowService.recordMessagePendingForwarding(component.getIdentifier(), messageFlowId);
                outboxService.recordEvent(messageFlowId,component.getIdentifier(), component.getRoute().getIdentifier(), component.getOwner());
            } else {
                messageFlowService.recordMessageNotForwarded(component.getIdentifier(), messageFlowId, result);
            }  
            
            // Final step is to delete the event from the inbox.
            inboxService.deleteEvent(eventId);
        } catch(Exception e) {
            throw new InboxEventSchedulerException(component.getIdentifier(),messageFlowId, e);
        }     
    }
}
