package integration.core.messaging.component.type.handler.filter;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.service.MessageFlowPropertyService;

/**
 * A message filter.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageAcceptancePolicy extends MessageFlowPolicy {
    
    @Autowired
    protected MessageFlowPropertyService propertyService;
}
