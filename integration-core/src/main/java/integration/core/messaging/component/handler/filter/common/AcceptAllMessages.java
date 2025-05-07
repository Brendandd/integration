package integration.core.messaging.component.handler.filter.common;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowDto;
import integration.core.messaging.component.handler.filter.FilterException;
import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * A message filter which accepts all messages (does not filter). This is the default behaviour.
 * 
 * @author Brendan Douglas
 *
 */
@Component("acceptAllMessages")
public class AcceptAllMessages extends MessageAcceptancePolicy {
    private static final String NAME = "Accept All Messages";

    @Override
    public MessageFlowPolicyResult applyPolicy(MessageFlowDto messageFlow) throws FilterException {
        return new MessageFlowPolicyResult(true);
    }

    @Override
    public String getName() {
        return NAME;
    }
    
    
}
