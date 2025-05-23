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
import integration.core.runtime.messaging.component.type.handler.BaseMessageHandlerComponent;

/**
 * Base class for all filter processing steps.
 */
@ComponentType(type = IntegrationComponentTypeEnum.FILTER)
public abstract class BaseFilterComponent extends BaseMessageHandlerComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFilterComponent.class);

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    public void defineAdditionalExceptionHandlers() {
        // Handle filter errors
        onException(FilterException.class)
        .process(exchange -> {           
            FilterException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, FilterException.class);
            getLogger().error("Filter exception - " + theException.toString());
            
            Long messageFlowId = getMessageFlowId(theException, exchange);
            
            if (!theException.isRetryable() && messageFlowId != null) {
                messagingFlowService.recordFilterError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true);         
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
                        long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        exchange.getMessage().setHeader(MESSAGE_FLOW_ID, parentMessageFlowId);
                                                
                        MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                                               
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowDto);
                                                                      
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            MessageFlowDto forwardedMessageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.PENDING_FORWARDING);
                            messageFlowEventService.recordMessageFlowEvent(forwardedMessageFlowDto.getId(),getIdentifier(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                        } else {
                            messagingFlowService.recordMessageNotForwarded(getIdentifier(), parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
                        }
                    }
                });
        }
}
