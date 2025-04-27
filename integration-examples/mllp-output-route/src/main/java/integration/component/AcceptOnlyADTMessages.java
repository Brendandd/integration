package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import integration.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.messaging.component.handler.filter.MessageForwardingPolicy;
import integration.messaging.hl7.component.handler.filter.MessageTypeFilter;

/**
 * A message type filter. Will only accept ADT^A04 messages.
 * 
 * @author Brendan Douglas
 */
@Component("acceptADT^A04")
public class AcceptOnlyADTMessages extends MessageTypeFilter {

    @Autowired
    @Qualifier("forwardAllMessages")
    private MessageForwardingPolicy messageForwardingPolicy;

    @Autowired
    @Qualifier("acceptAllMessages")
    private MessageAcceptancePolicy messageAcceptancePolicy;

    @Override
    public String[] getAllowedMessageTypes() {
        return new String[] { "ADT^A04" };
    }
}
