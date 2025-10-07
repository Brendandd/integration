package integration.messaging.hl7.component.adapter.mllp.outbound;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import integration.core.domain.IdentifierType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.BaseMessageFlowProcessor;
import integration.core.runtime.messaging.component.OutboxEventProcessor;
import integration.core.runtime.messaging.exception.retryable.OutboxEventSchedulerException;
import integration.core.runtime.messaging.service.OutboxService;


/**
 * Outbox event processor for an MLLP outbound adapter.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MLLPOutboundAdapterOutboxEventProcessor extends BaseMessageFlowProcessor<BaseMLLPOutboundAdapter> implements OutboxEventProcessor {
    
    @Autowired
    protected ProducerTemplate producerTemplate;
    
    @Autowired
    protected OutboxService outboxService;
    
    @Override
    public void process(Exchange exchange) throws Exception {
        MessageFlowDto messageFlowDto = null;
        Long messageFlowId = null;
        
        try {
            messageFlowId = exchange.getMessage().getBody(Long.class);
            exchange.getMessage().setHeader(IdentifierType.MESSAGE_FLOW_ID.name(), messageFlowId);
            messageFlowDto = messageFlowService.retrieveMessageFlow(messageFlowId, true);
            
            Long eventId = (Long)exchange.getMessage().getHeader(IdentifierType.EVENT_ID.name());
            
            outboxService.deleteEvent(eventId);
               
            String target = component.getTargetHost() + ":" + component.getTargetPort();
            String uri = "netty:tcp://" + target + component.constructAdapterOptions();
            
            messageFlowService.updatePendingForwardingToForwardedAction(messageFlowId);
            
            // Forward the message via MLLP.
            try {
                producerTemplate.sendBodyAndHeaders(uri, messageFlowDto.getMessageContent(), component.getHeaders(messageFlowDto));
            } catch(Exception e) {
                throw new MLLPForwardingException(eventId, component.getIdentifier(), messageFlowId, e);
            }
        } catch(Exception e) {
            throw new OutboxEventSchedulerException(component.getIdentifier(), messageFlowId, e);
        }         
    }
}
