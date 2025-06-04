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
 * A common Camel processor which is part of a components ingress.  It is called after the message flow id is read from the topic and then applies an acceptance policy.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IngressTopicConsumerWithAcceptancePolicyProcessor extends BaseMessageFlowProcessor<MessageConsumer> {
    
    @Autowired
    private OutboxService outboxService;

    @Override
    public void process(Exchange exchange) throws Exception {
        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
                                        
        MessageFlowPolicyResult result = component.getMessageAcceptancePolicy().applyPolicy(parentMessageFlowDto);
        if (result.isSuccess()) {
            // Record the content received by this component.
            MessageFlowDto acceptedMessageFlowDto = messageFlowService.recordMessageFlowWithSameContent(component.getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.ACCEPTED);
        
            // Record an event so the message can be forwarded to other components for processing.
            outboxService.recordEvent(acceptedMessageFlowDto.getId(),component.getIdentifier(), component.getRoute().getIdentifier(), component.getOwner(), OutboxEventType.INGRESS_COMPLETE); 
        } else {
            messageFlowService.recordMessageNotAccepted(component.getIdentifier(), parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_ACCEPTED);
        } 
    }
}
