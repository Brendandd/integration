package integration.core.messaging.component.handler.filter;

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
    private static final String NAME = "Forward All Messages";

    @Override
    public MessageFlowPolicyResult applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException {
        return new MessageFlowPolicyResult(true);
    }
    
    @Override
    public String getName() {
        return NAME;
    }
}
