package integration.core.runtime.messaging.component.type.adapter.smb;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNaming;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNamingStrategy;
import integration.core.runtime.messaging.exception.retryable.MessageForwardingException;
import integration.core.runtime.messaging.service.OutboxService;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SMBForwardingProcessor extends BaseMessageFlowProcessor<BaseSMBOutboundAdapter> {
    private static final String CAMEL_FILE_NAME = "CamelFileName";
    
    @Autowired
    private OutboxService outboxService;
    
    @Autowired
    private ProducerTemplate producerTemplate;
    
    @Autowired
    protected ApplicationContext springContext;
    
    @Override
    public void process(Exchange exchange) throws Exception {              
        // Delete the event.
        long eventId = (long)exchange.getMessage().getHeader(IdentifierType.OUTBOX_EVENT_ID.name());
        outboxService.deleteEvent(eventId);
    
        long messageFlowId = (Long)exchange.getMessage().getHeader(IdentifierType.MESSAGE_FLOW_ID.name());
        MessageFlowDto messageFlowDto = messageFlowService.retrieveMessageFlow(messageFlowId);
        
        // Change the status of the message flow from pending forwarding to forwarded
        messageFlowService.updateAction(messageFlowId, MessageFlowActionType.FORWARDED);
           
        Map<String, Object> headers = component.getHeaders(messageFlowDto);
        
        // Apply the file name strategy if the annotation exists.
        FileNaming annotation = component.getAnnotation(FileNaming.class);
         
        if (annotation != null) {
            FileNamingStrategy strategy = springContext.getBean(annotation.strategy(), FileNamingStrategy.class);
            
            String fileName = strategy.getFilename(exchange, messageFlowDto.getId());

            if (fileName != null) {
                headers.put(CAMEL_FILE_NAME, fileName);
            }
        }
        
        String uri = "smb:" + component.getHost() + "/" + component.getDestinationFolder() + component.constructAdapterOptions();
        
        try {
            producerTemplate.sendBodyAndHeaders(uri, messageFlowDto.getMessageContent(), headers);
        } catch(Exception e) {
            throw new MessageForwardingException("Error sending message via SMB component", eventId, component.getIdentifier(), messageFlowDto.getId(), e);
        }
    }
}
