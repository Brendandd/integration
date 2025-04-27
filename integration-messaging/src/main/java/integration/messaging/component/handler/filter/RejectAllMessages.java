package integration.messaging.component.handler.filter;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowStepDto;

/**
 * A message filter which filters all messages.
 * 
 * @author Brendan Douglas
 *
 */
@Component("rejectAllMessages")
public class RejectAllMessages extends MessageAcceptancePolicy {

    @Override
    public boolean applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException {
        return false;
    }
}
