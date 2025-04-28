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
 * Outbound route connector. Sends messages to other routes.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseOutboundRouteConnector extends BaseRouteConnector implements MessageConsumer  {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOutboundRouteConnector.class);
    
    protected List<MessageProducer> messageProducers = new ArrayList<>();

    public BaseOutboundRouteConnector(String componentName) throws Exception {
        super(componentName);
    }

    
    @Override
    public Logger getLogger() {
        return LOGGER;
    }




    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();

       
        // Creates one or more routes based on this components source components.  Each route reads from a topic. This is the entry point for outbound route connectors.
        for (MessageProducer messageProducer : messageProducers) {
            String componentPath = messageProducer.getIdentifier().getComponentPath();
          
            from("jms:VirtualTopic." + componentPath + "::Consumer." + identifier.getComponentPath() + ".VirtualTopic." + componentPath + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
                .routeId("messageReceiver-" + identifier.getComponentPath() + "-" + componentPath)
                .routeGroup(identifier.getComponentPath())
                .autoStartup(isInboundRunning)
                .transacted()
                    .process(new Processor() {
                    
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // Retrieve the inbound message.
                            long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                            MessageFlowStepDto parentMessageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                            
                            // Determine if the message should be accepted by this route connector.
                            MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(parentMessageFlowStepDto);
                            
                            if (result.isSuccess()) {
                                // Record the content received by this component.
                                MessageFlowStepDto messageFlowStepDto = messagingFlowService.recordMessageAccepted(BaseOutboundRouteConnector.this, parentMessageFlowStepId, getContentType());
                            
                                // Record an event so the message can be forwarded to other components for processing.
                                messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE);
                            } else {
                                messagingFlowService.recordMessageFiltered(BaseOutboundRouteConnector.this, parentMessageFlowStepId, getContentType());
                            }
                        }
                    });
        }

        
        // Entry point for a outbound route connector outbound message handling. 
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
                        MessageFlowStepDto parentMessageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
                                               
                        MessageFlowStepDto messageFlowStepDto = messagingFlowService.recordOutboundMessageHandlerComplete(parentMessageFlowStepDto.getMessageContent(), BaseOutboundRouteConnector.this, parentMessageFlowStepId, getContentType());
                        
                        messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
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
                        exchange.getMessage().setBody(messageFlowId, Long.class);
                    }
                })
            .to("jms:topic:VirtualTopic." + getName());
    }
}
