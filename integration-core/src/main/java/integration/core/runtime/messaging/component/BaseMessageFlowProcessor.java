package integration.core.runtime.messaging.component;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.IdentifierType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.service.MessageFlowService;

/**
 * Base processor for all camel processors called during message flows.
 * 
 * @param <T>
 */
public abstract class BaseMessageFlowProcessor<T extends MessagingComponent> implements Processor {
    
    @Autowired
    protected MessageFlowService messageFlowService;
    
    protected T component;

    
    public void setComponent(T component) {
        this.component = component;
    }
    
    
    protected MessageFlowDto getMessageFlowDtoFromExchangeBody(Exchange exchange, boolean includeMessage) throws MessageFlowProcessingException, MessageFlowNotFoundException {
        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
        exchange.getMessage().setHeader(IdentifierType.MESSAGE_FLOW_ID.name(), parentMessageFlowId);
        
        return messageFlowService.retrieveMessageFlow(parentMessageFlowId, includeMessage);
    }

}
