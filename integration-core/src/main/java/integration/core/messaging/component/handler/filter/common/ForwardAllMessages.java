package integration.core.messaging.component.handler.filter.common;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowDto;
import integration.core.messaging.component.handler.filter.FilterException;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;

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
