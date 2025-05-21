package integration.rest.service.impl;

import integration.core.exception.ComponentNotFoundException;
import integration.core.runtime.messaging.exception.retryable.ComponentAccessException;

/**
 * @author Brendan Douglas
 *
 */
public interface ComponentStateChangeService {
    
    StatusChangeResponse stopComponentInbound(long id) throws ComponentNotFoundException,ComponentAccessException; 
    
    StatusChangeResponse startComponentInbound(long id) throws ComponentNotFoundException,ComponentAccessException;  
    
    StatusChangeResponse stopComponentOutbound(long id) throws ComponentNotFoundException,ComponentAccessException; 
    
    StatusChangeResponse startComponentOutbound(long id) throws ComponentNotFoundException,ComponentAccessException; 
}


