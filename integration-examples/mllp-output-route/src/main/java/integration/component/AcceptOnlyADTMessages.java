package integration.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import integration.messaging.component.processingstep.filter.MessageAcceptancePolicy;
import integration.messaging.component.processingstep.filter.MessageForwardingPolicy;
import integration.messaging.hl7.component.processingstep.filter.MessageTypeFilter;

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

    @Override
    public String getFilterReason() {
        return "Only ADT messages are accepted. The message type is: " + messageType;
    }

    @Override
    public String getName() {
        return "Accept ADT Message Only Filter";
    }
}
