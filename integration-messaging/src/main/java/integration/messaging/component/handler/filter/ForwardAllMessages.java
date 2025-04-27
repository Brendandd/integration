package integration.messaging.component.handler.filter;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowStepDto;

/**
 * A message filter which accepts all messages (does not filter). This is the default behaviour.
 * 
 * @author Brendan Douglas
 *
 */
@Component("forwardAllMessages")
public class ForwardAllMessages extends MessageForwardingPolicy {

    @Override
    public boolean applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException {
        return true;
    }
}
