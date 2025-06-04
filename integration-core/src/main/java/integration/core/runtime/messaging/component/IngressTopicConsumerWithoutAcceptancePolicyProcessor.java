package integration.core.runtime.messaging.component;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.service.OutboxService;

/**
 * A common Camel processor which is part of a components ingress.  It is called after the message flow id is read from the topic and NO acceptance policy is applied.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IngressTopicConsumerWithoutAcceptancePolicyProcessor extends BaseMessageFlowProcessor<MessageProducer> {
    
    @Autowired
    private OutboxService outboxService;

    @Override
    public void process(Exchange exchange) throws Exception {
        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
        
        MessageFlowDto messageFlowDto  = messageFlowService.recordMessageFlowWithSameContent(component.getIdentifier(), parentMessageFlowDto.getId(),MessageFlowActionType.ACCEPTED);
        outboxService.recordEvent(messageFlowDto.getId(),component.getIdentifier(), component.getRoute().getIdentifier(), component.getOwner(), OutboxEventType.INGRESS_COMPLETE);  
    }
}
