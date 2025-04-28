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
    public MessageFlowPolicyResult applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException {
        MessageFlowPolicyResult result = new MessageFlowPolicyResult(false, "All messages are rejected by this policy");
        
        return result;
    }
}
