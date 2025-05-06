package integration.core.messaging.component.handler.filter.common;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowStepDto;
import integration.core.messaging.component.handler.filter.FilterException;
import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * A message filter which filters all messages.
 * 
 * @author Brendan Douglas
 *
 */
@Component("rejectAllMessages")
public class RejectAllMessages extends MessageAcceptancePolicy {
    private static final String NAME = "Reject All Messages";

    @Override
    public MessageFlowPolicyResult applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException {
        MessageFlowPolicyResult result = new MessageFlowPolicyResult(false, getName(), "All messages are rejected by this policy");
        
        return result;
    }
    
    @Override
    public String getName() {
        return NAME;
    }
}
