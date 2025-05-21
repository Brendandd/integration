package integration.core.runtime.messaging.component;

import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;

/**
 * A producer of a message.
 */
public interface MessageProducer extends MessagingComponent {
    
    /**
     * Adds a message consumer to this message producer.
     * 
     * @param messageConsumer
     */
    void addMessageConsumer(MessageConsumer messageConsumer);
    
    
    /**
     * Gets the message forwarding policy for this message producer.
     * 
     * @return
     * @throws RouteConfigurationException 
     * @throws  
     * @throws  
     */
    MessageForwardingPolicy getMessageForwardingPolicy() throws ComponentConfigurationException;
}
