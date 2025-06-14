package integration.core.runtime.messaging.component.type.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.runtime.messaging.component.BaseMessagingComponent;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.WriteToInboxProcessor;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.AcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.ForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;

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
@AcceptancePolicy(name = "acceptAllMessages")
@ForwardingPolicy(name = "forwardAllMessages")
public abstract class BaseMessageHandlerComponent extends BaseMessagingComponent implements MessageConsumer, MessageProducer {
    protected final List<MessageConsumer> messageConsumers = new ArrayList<>();
    protected final List<MessageProducer> messageProducers = new ArrayList<>();
    
    @Autowired
    protected WriteToInboxProcessor writeToInboxProcessor;
    
    @PostConstruct
    public void BaseMessageHabdlerComponentInit() {
        writeToInboxProcessor.setComponent(this);
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
    public MessageForwardingPolicy getMessageForwardingPolicy() throws ComponentConfigurationException {
        ForwardingPolicy annotation = getRequiredAnnotation(ForwardingPolicy.class);
          
        return springContext.getBean(annotation.name(), MessageForwardingPolicy.class);
    }

    
    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() throws ComponentConfigurationException {
        AcceptancePolicy annotation = getRequiredAnnotation(AcceptancePolicy.class);
               
        return springContext.getBean(annotation.name(), MessageAcceptancePolicy.class);
    }    

    
    @Override
    public void configureIngressRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        
        for (MessageProducer messageProducer : messageProducers) {
            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?concurrentConsumers=5&maxConcurrentConsumers=10")
            .routeId("ingress-" + getIdentifier() + "-" + messageProducer.getIdentifier())
            .routeGroup(getComponentPath())
            .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
            .transacted("jmsTransactionPolicy")
            .process(writeToInboxProcessor);
        } 
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        requiredAnnotations.add(AcceptancePolicy.class);
        requiredAnnotations.add(ForwardingPolicy.class);
    }
}
