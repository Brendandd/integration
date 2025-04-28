package integration.messaging.component.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowStepDto;
import integration.messaging.component.BaseMessagingComponent;
import integration.messaging.component.MessageConsumer;
import integration.messaging.component.MessageProducer;
import integration.messaging.component.handler.filter.MessageFlowPolicyResult;
import integration.messaging.service.MetaDataService;

/**
 * Base class for all message handlders. A message handler is a component which is not an inbound or outbound adapter.
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
    
    @Autowired
    protected MetaDataService metaDataService;
    
    public MessageHandler(String componentName) {
        super(componentName);
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
                            // Replace the message flow id with the actual message from the database.
                            Long parentMessageFlowStepId = exchange.getMessage().getBody(Long.class);
                            MessageFlowStepDto parentMessageFlowStepDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowStepId);
 
                            MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(parentMessageFlowStepDto);
                            if (result.isSuccess()) {
                                // Record the content received by this component.
                                MessageFlowStepDto messageFlowStepDto = messagingFlowService.recordMessageAccepted(MessageHandler.this, parentMessageFlowStepId, getContentType());
                            
                                // Record an event so the message can be forwarded to other components for processing.
                                messagingFlowService.recordMessageFlowEvent(messageFlowStepDto.getId(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE); 
                            } else {
                                messagingFlowService.recordMessageFiltered(MessageHandler.this, parentMessageFlowStepId, result);
                            }
                        }
                    });
        }
    }
}
