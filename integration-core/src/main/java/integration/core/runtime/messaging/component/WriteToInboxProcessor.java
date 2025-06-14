package integration.core.runtime.messaging.component;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.service.InboxService;

/**
 * Writes a message to the inbox once received from JMS.  
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WriteToInboxProcessor extends BaseMessageFlowProcessor<MessagingComponent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WriteToInboxProcessor.class);
    
    @Autowired
    private InboxService inboxService;

    @Override
    public void process(Exchange exchange) throws Exception {          
        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange, true);
        
        String jmsMessageId = (String)exchange.getMessage().getHeader("JMSMessageID");
        
        inboxService.recordEvent(parentMessageFlowDto.getId(),component.getIdentifier(), component.getRoute().getIdentifier(),component.getOwner(), jmsMessageId); 
    }
}
