package integration.messaging.component;

import integration.messaging.component.handler.filter.MessageForwardingPolicy;

/**
 * A producer of a message.
 */
public interface MessageProducer extends Component {
    
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
