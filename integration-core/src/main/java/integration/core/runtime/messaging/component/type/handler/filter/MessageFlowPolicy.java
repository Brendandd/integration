package integration.core.runtime.messaging.component.type.handler.filter;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.service.MessageFlowPropertyService;

public abstract class MessageFlowPolicy {
    
    @Autowired
    protected MessageFlowPropertyService messageFlowPropertyService;
    
    public abstract MessageFlowPolicyResult applyPolicy(MessageFlowDto messageFlow) throws FilterException;
    
    public abstract String getName();
}
