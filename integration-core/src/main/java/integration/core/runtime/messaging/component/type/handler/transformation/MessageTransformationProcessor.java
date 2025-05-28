package integration.core.runtime.messaging.component.type.handler.transformation;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.service.MessageFlowService;
import integration.core.runtime.messaging.service.OutboxService;

/**
 * The message transformation processor.  Transforms and then creates a processing complete event.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageTransformationProcessor implements Processor {

    @Autowired
    private OutboxService outboxService;
    
    @Autowired
    private MessageFlowService messageFlowService;
    
    private BaseTransformationComponent component;

    
    public void setComponent(BaseTransformationComponent component) {
        this.component = component;
    }
    
    
    @Override
    public void process(Exchange exchange) throws Exception {
        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
        
        // Transform the content.
        String transformedContent = component.getTransformer().transform(parentMessageFlowDto);
        
        MessageFlowDto transformedMessageFlowDto = messageFlowService.recordNewContentMessageFlow(transformedContent, component.getIdentifier(),parentMessageFlowDto.getId(), component.getContentType(), MessageFlowActionType.TRANSFORMED);
        outboxService.recordEvent(transformedMessageFlowDto.getId(),component.getIdentifier(), OutboxEventType.PROCESSING_COMPLETE); 
    }

    
    private MessageFlowDto getMessageFlowDtoFromExchangeBody(Exchange exchange) throws MessageFlowProcessingException, MessageFlowNotFoundException {
        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
        exchange.getMessage().setHeader(IdentifierType.MESSAGE_FLOW_ID.name(), parentMessageFlowId);
        
        return messageFlowService.retrieveMessageFlow(parentMessageFlowId);
    }
}
