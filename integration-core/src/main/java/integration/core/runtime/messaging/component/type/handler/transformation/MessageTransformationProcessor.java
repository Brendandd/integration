package integration.core.runtime.messaging.component.type.handler.transformation;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.service.OutboxService;

/**
 * The message transformation processor.  Transforms and then creates a processing complete event.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageTransformationProcessor extends BaseMessageFlowProcessor<BaseTransformationComponent> {

    @Autowired
    private OutboxService outboxService;

    @Override
    public void process(Exchange exchange) throws Exception {
        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange, true);
        
        // Transform the content.
        String transformedContent = component.getTransformer().transform(parentMessageFlowDto);
        
        MessageFlowDto transformedMessageFlowDto = messageFlowService.recordNewContentMessageFlow(transformedContent, component.getIdentifier(),parentMessageFlowDto.getId(), component.getContentType(), MessageFlowActionType.TRANSFORMED);
        outboxService.recordEvent(transformedMessageFlowDto.getId(),component.getIdentifier(), component.getRoute().getIdentifier(),component.getOwner(), OutboxEventType.PROCESSING_COMPLETE); 
    }
}
