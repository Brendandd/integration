package integration.core.runtime.messaging.component.type.handler.splitter;

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
 * The message splitter processor.  Splits the message into zero or more messages and then creates an processing complete event for each.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageSplitterProcessor implements Processor {
    
    @Autowired
    private OutboxService outboxService;
    
    @Autowired
    private MessageFlowService messageFlowService;
    
    private BaseSplitterComponent component;

    public void setComponent(BaseSplitterComponent component) {
        this.component = component;
    }
    
    @Override
    public void process(Exchange exchange) throws Exception {
        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
        
        String[] splitMessages = component.getSplitter().split(parentMessageFlowDto);
                  
        for (int i = 0; i < splitMessages.length; i++) {
            MessageFlowDto splitMessageFlowDto = messageFlowService.recordMessageFlowWithSameContent(component.getIdentifier(),parentMessageFlowDto.getId(), MessageFlowActionType.CREATED_FROM_SPLIT);
            outboxService.recordEvent(splitMessageFlowDto.getId(),component.getIdentifier(), OutboxEventType.PROCESSING_COMPLETE); 
        }
    }

    
    private MessageFlowDto getMessageFlowDtoFromExchangeBody(Exchange exchange) throws MessageFlowProcessingException, MessageFlowNotFoundException {
        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
        exchange.getMessage().setHeader(IdentifierType.MESSAGE_FLOW_ID.name(), parentMessageFlowId);
        
        return messageFlowService.retrieveMessageFlow(parentMessageFlowId);
    }
}
