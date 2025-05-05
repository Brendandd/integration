package integration.core.messaging.component;

import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;

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
     */
    MessageForwardingPolicy getMessageForwardingPolicy();
}
