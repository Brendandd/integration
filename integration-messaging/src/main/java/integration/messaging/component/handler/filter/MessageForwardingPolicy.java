package integration.messaging.component.handler.filter;

import org.springframework.beans.factory.annotation.Autowired;

import integration.messaging.service.MetaDataService;

/**
 * A message filter.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageForwardingPolicy extends MessageFlowPolicy {
    public static final String FORWARD_MESSAGE = "FORWARD_MESSAGE";
    
    @Autowired
    protected MetaDataService metaDataService;
}
