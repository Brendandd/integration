package integration.core.runtime.messaging.service;

import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;

/**
 * Services to access message flow properties.
 */
public interface MessageFlowPropertyService {
    
    /**
     * Gets a property associated with the supplied message flow.
     * 
     * @param key
     * @param messageFlowId
     * @return
     * @throws MessageFlowProcessingException
     */
    Object getPropertyValue(String key, long messageFlowId) throws MessageFlowProcessingException;
    
    
    /**
     * Add a property to the message flow.
     * 
     * @param key
     * @param value
     * @param messageFlowId
     * @throws MessageFlowProcessingException
     */
    void addProperty(String key, Object value, long messageFlowId) throws MessageFlowProcessingException;
}
