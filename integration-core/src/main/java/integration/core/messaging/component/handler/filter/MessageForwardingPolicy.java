package integration.core.messaging.component.handler.filter;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.service.MessageFlowPropertyService;

/**
 * A message filter.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageForwardingPolicy extends MessageFlowPolicy {
    public static final String FORWARD_MESSAGE = "FORWARD_MESSAGE";
    
    @Autowired
    protected MessageFlowPropertyService messagePropertyService;
}
