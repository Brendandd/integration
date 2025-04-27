package integration.messaging.component.handler.filter;

import integration.core.dto.MessageFlowStepDto;

public abstract class MessageFlowPolicy {
    public abstract boolean applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException;
}
