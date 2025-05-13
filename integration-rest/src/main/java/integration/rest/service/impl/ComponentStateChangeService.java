package integration.rest.service.impl;

import integration.core.exception.ConfigurationException;

/**
 * @author Brendan Douglas
 *
 */
public interface ComponentStateChangeService {
    
    StatusChangeResponse stopComponentInbound(long id) throws ConfigurationException;
    
    StatusChangeResponse startComponentInbound(long id) throws ConfigurationException;  
    
    StatusChangeResponse stopComponentOutbound(long id) throws ConfigurationException;
    
    StatusChangeResponse startComponentOutbound(long id) throws ConfigurationException;
}


