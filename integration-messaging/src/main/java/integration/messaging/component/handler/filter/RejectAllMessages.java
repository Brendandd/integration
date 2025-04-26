package integration.messaging.component.handler.filter;

import org.springframework.stereotype.Component;

/**
 * A message filter which filters all messages.
 * 
 * @author Brendan Douglas
 *
 */
@Component("rejectAllMessages")
public class RejectAllMessages extends MessageAcceptancePolicy {

    @Override
    public boolean applyPolicy(String messageContent) throws FilterException {
        return false;
    }

    @Override
    public String getFilterReason() {
        return "All messages are filtered by filter";
    }

    @Override
    public String getName() {
        return "Filter All Messages Filter";
    }
}
