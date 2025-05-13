package integration.core.runtime.messaging.component;

import integration.core.exception.ConfigurationException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;

/**
 * A consumer of a message.
 */
public interface MessageConsumer extends MessagingComponent {
    
    /**
     * Adds a message producer to this message consumer.
     * 
     * @param messageProducer
     */
    public void addMessageProducer(MessageProducer messageProducer);
    
    
    /**
     * Gets the message acceptance policy for this message consumer.
     * 
     * @return
     */
    MessageAcceptancePolicy getMessageAcceptancePolicy() throws ConfigurationException;
}
