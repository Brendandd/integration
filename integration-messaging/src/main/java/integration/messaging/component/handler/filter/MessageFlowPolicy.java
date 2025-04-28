package integration.messaging.component.handler.filter;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.dto.MessageFlowStepDto;
import integration.messaging.service.MetaDataService;

public abstract class MessageFlowPolicy {
    
    @Autowired
    protected MetaDataService metaDataService;
    
    public abstract MessageFlowPolicyResult applyPolicy(MessageFlowStepDto messageFlowStep) throws FilterException;
}
