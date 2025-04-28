package integration.messaging.component.handler.filter;

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

    @Override
    public MessageFlowPolicyResult applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException {
        return new MessageFlowPolicyResult(true);
    }
}
