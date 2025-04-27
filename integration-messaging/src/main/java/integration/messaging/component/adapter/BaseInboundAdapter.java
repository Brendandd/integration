package integration.messaging.component.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.messaging.component.BaseMessagingComponent;
import integration.messaging.component.MessageConsumer;
import integration.messaging.component.MessageProducer;

/**
 * Base class for all inbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseInboundAdapter extends BaseAdapter implements MessageProducer {
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    public BaseInboundAdapter(String componentName) {
        super(componentName);
    }

    
    @Override
    public void addMessageConsumer(MessageConsumer messageConsumer) {
        if (!messageConsumers.contains(messageConsumer)) {
            this.messageConsumers.add(messageConsumer);
            messageConsumer.addMessageProducer(this);
        }
    }

    
    /**
     * 
     * 
     * @return
     */
    public abstract String getFromUriString();

    
    @Override
    public void configure() throws Exception {
        super.configure();
                
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
                        String messageContent = messagingFlowService.retrieveMessageContent(parentMessageFlowStepId);
                                               
                        boolean forwardMessage = getMessageForwardingPolicy().applyPolicy(messageContent);
                                  
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (forwardMessage) {
                            long newMessageFlowStepId = messagingFlowService.recordMessageDispatchedByOutboundHandler(messageContent, BaseInboundAdapter.this, parentMessageFlowStepId, getContentType());
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
                        Long messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_STEP_ID);
                        exchange.getMessage().setBody(messageFlowId);
                    }
                })
            .to("jms:topic:VirtualTopic." + identifier.getComponentPath());
    }
}

