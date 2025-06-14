package integration.core.runtime.messaging.component.type.connector.outbound;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Inbox event processor for outbound route connectors.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OutboundRouteConnectorInboxEventProcessor extends BaseMessageFlowProcessor<BaseOutboundRouteConnectorComponent> implements InboxEventProcessor{
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundRouteConnectorInboxEventProcessor.class);
    
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
                MessageFlowDto pendingForwardingFlowDto = messageFlowService.recordMessagePendingForwarding(component.getIdentifier(), acceptedMessageFlowDto.getId());
                                  
                outboxService.recordEvent(pendingForwardingFlowDto.getId(),component.getIdentifier(), component.getRoute().getIdentifier(), component.getOwner()); 
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
