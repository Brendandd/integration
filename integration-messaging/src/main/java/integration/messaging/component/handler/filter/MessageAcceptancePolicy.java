package integration.messaging.component.handler.filter;

import org.springframework.beans.factory.annotation.Autowired;

import integration.messaging.service.MetaDataService;

/**
 * A message filter.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class MessageAcceptancePolicy extends MessageFlowPolicy {
    
    @Autowired
    protected MetaDataService metaDataService;
}
