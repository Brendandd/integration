package integration.core.messaging.component.handler.filter;

import org.springframework.stereotype.Component;

import integration.core.dto.MessageFlowStepDto;

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
    public MessageFlowPolicyResult applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException {
        return new MessageFlowPolicyResult(true);
    }

    @Override
    public String getName() {
        return NAME;
    }
    
    
}
