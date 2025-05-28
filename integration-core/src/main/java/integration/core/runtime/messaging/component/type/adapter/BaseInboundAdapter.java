package integration.core.runtime.messaging.component.type.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.EgressQueueConsumerWithForwardingPolicyProcessor;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.ForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.exception.retryable.MessageForwardingException;
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
    
    @PostConstruct
    public void BaseInboundAdapterInit() {
        egressQueueConsumerWithForwardingPolicyProcessor.setComponent(this);
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
    protected void forwardMessage(Exchange exchange, MessageFlowDto messageFlowDto, long eventId) throws MessageForwardingException {
        try {
            producerTemplate.sendBody("jms:topic:VirtualTopic." + getComponentPath(), messageFlowDto.getId());
        } catch(Exception e) {
            throw new MessageForwardingException("Error forwarding message out of component", eventId, getIdentifier(), messageFlowDto.getId(), e);
        }
    }
    
    
    @Override
    protected void configureEgressQueueConsumerRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        from("jms:queue:egressQueue-" + getIdentifier())
            .routeId("egressQueue-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .transacted()
                .process(egressQueueConsumerWithForwardingPolicyProcessor); 
    }

    
    @Override
    protected void configureRequiredAnnotations() {            
        requiredAnnotations.add(ForwardingPolicy.class);
    }
}

