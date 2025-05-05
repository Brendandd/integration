package integration.core.messaging.component.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentState;
import integration.core.domain.configuration.ComponentType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.dto.MessageFlowStepDto;
import integration.core.exception.EventProcessingException;
import integration.core.messaging.component.BaseMessagingComponent;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Inbound route connector. Accepts messages from other routes.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseInboundRouteConnector extends BaseRouteConnector implements MessageProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseInboundRouteConnector.class);
    
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    public void addMessageConsumer(MessageConsumer messageConsumer) {
        if (!messageConsumers.contains(messageConsumer)) {
            this.messageConsumers.add(messageConsumer);
            messageConsumer.addMessageProducer(this);
        }
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.INBOUND_ROUTE_CONNECTOR;
    }

    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.INBOUND_ROUTE_CONNECTOR;
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();

        from("jms:VirtualTopic." + getConnectorName() + "::Consumer." + getComponentPath() + ".VirtualTopic." + getName() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("messageReceiver-" + getComponentPath() + "-" + getName())
            .routeGroup(getComponentPath())
            .autoStartup(inboundState == ComponentState.RUNNING)
        .transacted()
            .process(new Processor() {
                
                @Override
                public void process(Exchange exchange) throws Exception {
                    Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                                                          
                    MessageFlowStepDto messageFlowStepDto  = messagingFlowService.recordMessageFlowStep(BaseInboundRouteConnector.this, parentMessageFlowStepId, null,MessageFlowStepActionType.MESSAGE_RECEIVED_FROM_ANOTHER_ROUTE);
                    
                    messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE);               }
            });
 
        
        // Entry point for an inbound adapters outbound message handling. 
        from("direct:outboundMessageHandling-" + getComponentPath())
            .routeId("outboundMessageHandling-" + getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Record the outbound message.
                        Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                        MessageFlowStepDto parentMessageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                                               
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowStepDto);
                                                                       
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            MessageFlowStepDto messageFlowStepDto = messagingFlowService.recordMessageFlowStep(BaseInboundRouteConnector.this,parentMessageFlowStepId, null, MessageFlowStepActionType.MESSAGE_FORWARDED);
                            messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE);
                        } else {
                            messagingFlowService.recordMessageNotForwarded(BaseInboundRouteConnector.this, parentMessageFlowStepDto.getId(), result, MessageFlowStepActionType.MESSAGE_NOT_FORWARDED);
                        }
                    }
                });

        
        // A route to process outbound message handling complete events.  This is the final stage of an inbound adapter.
        from("direct:handleOutboundMessageHandlingCompleteEvent-" + getComponentPath())
            .routeId("handleOutboundMessageHandlingCompleteEvent-" + getComponentPath())
            .routeGroup(getComponentPath())
            .transacted()
            .process(new Processor() {

                @Override
                public void process(Exchange exchange) throws Exception {   
                    Long eventId = null;
                    Long messageFlowId = null;
                    
                    try {
                        // Delete the event.
                        eventId = (long)exchange.getMessage().getHeader(BaseMessagingComponent.EVENT_ID);
                        messagingFlowService.deleteEvent(eventId);
                    
                        // Set the message flow step id as the exchange message body so it can be added to the queue.
                        messageFlowId = (Long)exchange.getMessage().getHeader(MESSAGE_FLOW_STEP_ID);
                        
                        producerTemplate.sendBody("jms:topic:VirtualTopic." + getComponentPath(), messageFlowId);
                    } catch(Exception e) {
                        throw new EventProcessingException("Error adding message step flow id to the topic", eventId, messageFlowId, getComponentPath(), e);
                    }
                }
            });
    }
}
