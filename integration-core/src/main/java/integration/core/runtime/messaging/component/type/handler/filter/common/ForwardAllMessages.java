package integration.core.runtime.messaging.component.type.handler.filter.common;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;

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
    public MessageFlowPolicyResult applyPolicy(MessageFlowDto messageFlow) throws FilterException {
        return new MessageFlowPolicyResult(true);
    }
    
    @Override
    public String getName() {
        return NAME;
    }
}
