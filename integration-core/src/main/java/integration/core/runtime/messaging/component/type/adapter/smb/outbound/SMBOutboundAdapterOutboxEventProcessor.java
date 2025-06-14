package integration.core.runtime.messaging.component.type.adapter.smb.outbound;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.component.OutboxEventProcessor;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNaming;
import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNamingStrategy;
import integration.core.runtime.messaging.exception.retryable.OutboxEventSchedulerException;
import integration.core.runtime.messaging.service.OutboxService;


/**
 * Outbox event processor for an SMB outbound adapter.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SMBOutboundAdapterOutboxEventProcessor extends BaseMessageFlowProcessor<BaseSMBOutboundAdapter> implements OutboxEventProcessor {
    private static final String CAMEL_FILE_NAME = "CamelFileName";
    
    @Autowired
    protected ApplicationContext springContext;
    
    @Autowired
    protected ProducerTemplate producerTemplate;
    
    @Autowired
    protected OutboxService outboxService;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange, true);
            
            Long eventId = (Long)exchange.getMessage().getHeader(IdentifierType.OUTBOX_EVENT_ID.name());
            
            outboxService.deleteEvent(eventId);
            
            Map<String, Object> headers = component.getHeaders(parentMessageFlowDto);
            
            // Apply the file name strategy if the annotation exists.
            FileNaming annotation = component.getAnnotation(FileNaming.class);
             
            if (annotation != null) {
                FileNamingStrategy strategy = springContext.getBean(annotation.strategy(), FileNamingStrategy.class);
                
                String fileName = strategy.getFilename(exchange, parentMessageFlowDto.getId());
    
                if (fileName != null) {
                    headers.put(CAMEL_FILE_NAME, fileName);
                }
            }
            
            messageFlowService.updatePendingForwardingToForwardedAction(parentMessageFlowDto.getId());
               
            String uri = "smb:" + component.getHost() + "/" + component.getDestinationFolder() + component.constructAdapterOptions();
            
            // Forward the message via SMB.
            try {
                producerTemplate.sendBodyAndHeaders(uri, parentMessageFlowDto.getMessageContent(), headers);
            } catch(Exception e) {
                throw new SMBForwardingException(eventId, component.getIdentifier(), parentMessageFlowDto.getId(), e);
            }
        } catch(Exception e) {
            throw new OutboxEventSchedulerException(component.getIdentifier(), e);
        }         
    }
}
