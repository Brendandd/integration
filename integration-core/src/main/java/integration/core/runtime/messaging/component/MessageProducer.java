package integration.core.runtime.messaging.component;

import integration.core.exception.ConfigurationException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;

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
     * @throws ConfigurationException 
     */
    MessageForwardingPolicy getMessageForwardingPolicy() throws ConfigurationException;
}
