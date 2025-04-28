package integration.messaging.component.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowStepDto;
import integration.messaging.component.MessageConsumer;
import integration.messaging.component.MessageProducer;
import integration.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Inbound route connector. Accepts messages from other routes.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseInboundRouteConnector extends BaseRouteConnector implements MessageProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseInboundRouteConnector.class);
    
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    public BaseInboundRouteConnector(String componentName) {
        super(componentName);
    }

    
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
    public void configure() throws Exception {
        super.configure();

            
        from("jms:VirtualTopic." + getName() + "::Consumer." + identifier.getComponentPath() + ".VirtualTopic." + getName() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("messageReceiver-" + identifier.getComponentPath() + "-" + getName())
            .routeGroup(identifier.getComponentPath())
            .autoStartup(isInboundRunning)
        .transacted()
            .process(new Processor() {
                
                @Override
                public void process(Exchange exchange) throws Exception {
                    Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                                                          
                    Long newMessageFlowStepId = messagingFlowService.recordInboundMessageProducedByOtherRoute(BaseInboundRouteConnector.this, parentMessageFlowStepId, getContentType());
                    
                    messagingFlowService.recordMessageFlowEvent(newMessageFlowStepId, MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE);               }
            });
 
        
        // Entry point for an inbound adapters outbound message handling. 
        from("direct:outboundMessageHandling-" + identifier.getComponentPath())
            .routeId("outboundMessageHandling-" + identifier.getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(identifier.getComponentPath())
            .autoStartup(isInboundRunning)
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Record the outbound message.
                        Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                        MessageFlowStepDto messageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                                               
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(messageFlowStepDto);
                                                                       
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            long newMessageFlowStepId = messagingFlowService.recordMessageDispatchedByOutboundHandler(messageFlowStepDto.getMessageContent(), BaseInboundRouteConnector.this,parentMessageFlowStepId, getContentType());
                            messagingFlowService.recordMessageFlowEvent(newMessageFlowStepId, MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE);
                        } else {
                            // filter message.
                        }
                    }
                });

        
        // A route to process outbound message handling complete events.  This is the final stage of an inbound adapter.
        from("direct:handleOutboundMessageHandlingCompleteEvent-" + identifier.getComponentPath())
            .routeId("handleOutboundMessageHandlingCompleteEvent-" + identifier.getComponentPath())
            .routeGroup(identifier.getComponentPath())
            .transacted()
                .process(new Processor() {
    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Delete the event.
                        Long eventId = exchange.getMessage().getBody(Long.class);
                        messagingFlowService.deleteEvent(eventId);
                        
                        // Set the message flow step id as the exchange message body so it can be added to the queue.
                        Long messageFlowId = (Long)exchange.getMessage().getHeader(MESSAGE_FLOW_STEP_ID);
                        exchange.getMessage().setBody(messageFlowId);
                    }
                })
            .to("jms:topic:VirtualTopic." + identifier.getComponentPath());
    }
}
