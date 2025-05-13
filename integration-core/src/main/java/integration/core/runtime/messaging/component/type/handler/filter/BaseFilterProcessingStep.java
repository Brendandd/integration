package integration.core.runtime.messaging.component.type.handler.filter;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.handler.MessageHandler;

/**
 * Base class for all filter processing steps.
 */
@ComponentType(type = IntegrationComponentTypeEnum.FILTER)
public abstract class BaseFilterProcessingStep extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFilterProcessingStep.class);

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();


        // Entry point for an inbound adapters outbound message handling. 
        from("direct:outboundMessageHandling-" + getIdentifier())
            .routeId("outboundMessageHandling-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Record the outbound message.
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                                               
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowDto);
                                                                      
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            MessageFlowDto forwardedMessageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.PENDING_FORWARDING);
                            messagingFlowService.recordMessageFlowEvent(forwardedMessageFlowDto.getId(),getIdentifier(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                        } else {
                            messagingFlowService.recordMessageNotForwarded(getIdentifier(), parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
                        }
                    }
                });
        }
}
