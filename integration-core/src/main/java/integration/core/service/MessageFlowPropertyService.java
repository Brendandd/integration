package integration.core.service;

import integration.core.messaging.MessageFlowException;

public interface MessageFlowPropertyService {
    
    /**
     * Gets a property associated with the supplied message flow.
     * 
     * @param key
     * @param messageFlowId
     * @return
     */
    String getPropertyValue(String key, long messageFlowId) throws MessageFlowException;
    
    /**
     * Add a property to the message flow.
     * 
     * @param key
     * @param value
     * @param messageFlowId
     */
    void addProperty(String key, String value, long messageFlowId) throws MessageFlowException;
}
