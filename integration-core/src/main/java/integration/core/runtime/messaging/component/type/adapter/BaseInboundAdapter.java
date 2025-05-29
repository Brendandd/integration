package integration.core.runtime.messaging.component.type.adapter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.runtime.messaging.component.EgressQueueConsumerWithForwardingPolicyProcessor;
import integration.core.runtime.messaging.component.IntraRouteJMSTopicProducerEgressForwardingProcessor;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.ForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all inbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
@ForwardingPolicy(name = "forwardAllMessages")
public abstract class BaseInboundAdapter extends BaseAdapter implements MessageProducer {
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();
    
    @Autowired
    private EgressQueueConsumerWithForwardingPolicyProcessor egressQueueConsumerWithForwardingPolicyProcessor;
    
    @Autowired
    protected IntraRouteJMSTopicProducerEgressForwardingProcessor intraRouteJMSTopicProducerEgressForwardingProcessor;
    
    @PostConstruct
    public void BaseInboundAdapterInit() {
        egressQueueConsumerWithForwardingPolicyProcessor.setComponent(this);
        intraRouteJMSTopicProducerEgressForwardingProcessor.setComponent(this);
    }

    
    @Override
    public void addMessageConsumer(MessageConsumer messageConsumer) {
        if (!messageConsumers.contains(messageConsumer)) {
            this.messageConsumers.add(messageConsumer);
            messageConsumer.addMessageProducer(this);
        }
    }

    
    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() throws ComponentConfigurationException {
        ForwardingPolicy annotation = getRequiredAnnotation(ForwardingPolicy.class);
               
        return springContext.getBean(annotation.name(), MessageForwardingPolicy.class);
    }

    
    /**
     * Where to get the message from.  This is a Camel URI.
     * 
     * 
     * @return
     */
    public abstract String getFromUriString();

    
    @Override
    protected EgressQueueConsumerWithForwardingPolicyProcessor getEgressQueueConsumerProcessor() throws ComponentConfigurationException, RouteConfigurationException {
        return egressQueueConsumerWithForwardingPolicyProcessor; 
    }
    
    
    @Override
    protected IntraRouteJMSTopicProducerEgressForwardingProcessor getEgressForwardingProcessor() throws ComponentConfigurationException, RouteConfigurationException {
        return intraRouteJMSTopicProducerEgressForwardingProcessor;
    }

    
    @Override
    protected void configureRequiredAnnotations() {            
        requiredAnnotations.add(ForwardingPolicy.class);
    }
}

