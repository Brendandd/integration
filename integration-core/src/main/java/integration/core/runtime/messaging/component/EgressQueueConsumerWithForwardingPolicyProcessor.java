package integration.core.runtime.messaging.component;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.service.OutboxService;

/**
 * A common Camel processor which is called after a message flow id is consaumed from the egress queue.  A forwarding policy is applied before either filtering the message or writing an event for forwarding.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EgressQueueConsumerWithForwardingPolicyProcessor extends BaseMessageFlowProcessor<MessageProducer> {
    
    @Autowired
    private OutboxService outboxService;

    @Override
    public void process(Exchange exchange) throws Exception {
        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
        
        MessageFlowPolicyResult result = component.getMessageForwardingPolicy().applyPolicy(parentMessageFlowDto);
                                                       
        // Apply the message forwarding rules and either write an event for further processing or filter the message.
        if (result.isSuccess()) {
            MessageFlowDto forwardedMessageFlowDto = messageFlowService.recordMessageFlowWithSameContent(component.getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.PENDING_FORWARDING);
            outboxService.recordEvent(forwardedMessageFlowDto.getId(),component.getIdentifier(), component.getRoute().getIdentifier(), component.getOwner(), OutboxEventType.PENDING_FORWARDING);
        } else {
            messageFlowService.recordMessageNotForwarded(component.getIdentifier(), parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
        }  
    }
}
