package integration.core.runtime.messaging.component.type.adapter.inbound;

import java.util.ArrayList;
import java.util.List;

import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.type.adapter.BaseAdapter;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.ForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;

/**
 * Base class for all inbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
@ForwardingPolicy(name = "forwardAllMessages")
public abstract class BaseInboundAdapter extends BaseAdapter implements MessageProducer {
    protected final List<MessageConsumer> messageConsumers = new ArrayList<>();
    
    
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
    protected void configureRequiredAnnotations() {            
        requiredAnnotations.add(ForwardingPolicy.class);
    }
}

