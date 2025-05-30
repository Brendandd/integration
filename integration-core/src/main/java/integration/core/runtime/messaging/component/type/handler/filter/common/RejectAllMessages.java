package integration.core.runtime.messaging.component.type.handler.filter.common;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;

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
    public MessageFlowPolicyResult applyPolicy(MessageFlowDto messageFlow) throws FilterException {
        return new MessageFlowPolicyResult(false, getName(), "All messages are rejected by this policy");
    }
    
    @Override
    public String getName() {
        return NAME;
    }
}
