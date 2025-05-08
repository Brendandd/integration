package integration.core.messaging.component.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.core.domain.configuration.ComponentState;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.BaseMessagingComponent;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;
import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;

/**
 * Base class for all message handlers. A message handler is a component which is not an inbound or outbound adapter.
 * 
 * A processing step allows both the incoming and outgoing messages to be
 * filtered. By default all messages are allowed to be processed.
 * 
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageHandler extends BaseMessagingComponent implements MessageConsumer, MessageProducer {
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();
    protected List<MessageProducer> messageProducers = new ArrayList<>();
        
    @Override
    public String getMessageForwardingUriString() throws ConfigurationException {
        return "jms:topic:VirtualTopic." + getComponentPath();
    }

    @Override
    public void addMessageConsumer(MessageConsumer messageConsumer) {
        if (!messageConsumers.contains(messageConsumer)) {
            this.messageConsumers.add(messageConsumer);
            messageConsumer.addMessageProducer(this);
        }
    }
    
    
    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }

    
    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        ForwardingPolicy annotation = this.getClass().getAnnotation(ForwardingPolicy.class);
               
        if (annotation == null) {
            return springContext.getBean("forwardAllMessages", MessageForwardingPolicy.class);
        }
        
        return springContext.getBean(annotation.name(), MessageForwardingPolicy.class);
    }

    
    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        AcceptancePolicy annotation = this.getClass().getAnnotation(AcceptancePolicy.class);
               
        if (annotation == null) {
            return springContext.getBean("acedptAllMessages", MessageAcceptancePolicy.class);
        }
        
        return springContext.getBean(annotation.name(), MessageAcceptancePolicy.class);
    }    

    
    @Override
    protected Long getBodyContent(MessageFlowDto messageFlowDto) {
        return messageFlowDto.getId();
    }
    
    
    @Override
    public void configure() throws Exception {
        super.configure();

        // Creates one or more routes based on this components source components.  Each route reads from a topic. This is the entry point for outbound route connectors.
        for (MessageProducer messageProducer : messageProducers) {

            
            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
                .routeId("inboundEntryPoint-" + getComponentPath() + "-" + messageProducer.getComponentPath())
                .routeGroup(getComponentPath())
                .autoStartup(inboundState == ComponentState.RUNNING)
                .transacted()
                    .process(new Processor() {
                    
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // Replace the message flow id with the actual message from the database.
                            Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                            MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                            
                            MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(parentMessageFlowDto);
                            if (result.isSuccess()) {
                                // Record the content received by this component.
                                MessageFlowDto acceptedMessageFlowDto = messagingFlowService.recordMessageFlow(MessageHandler.this, parentMessageFlowId, MessageFlowActionType.ACCEPTED);
                            
                                // Record an event so the message can be forwarded to other components for processing.
                                messagingFlowService.recordMessageFlowEvent(acceptedMessageFlowDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                            } else {
                                messagingFlowService.recordMessageNotAccepted(MessageHandler.this, parentMessageFlowId, result, MessageFlowActionType.NOT_ACCEPTED);
                            }
                        }
                    });
        }
    }
}
