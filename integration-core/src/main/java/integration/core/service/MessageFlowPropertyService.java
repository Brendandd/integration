package integration.core.service;

public interface MessageFlowPropertyService {
    
    /**
     * Gets a property associated with the supplied message flow.
     * 
     * @param key
     * @param messageFlowId
     * @return
     */
    String getPropertyValue(String key, long messageFlowId);
    
    /**
     * Add a property to the message flow.
     * 
     * @param key
     * @param value
     * @param messageFlowId
     */
    void addProperty(String key, String value, long messageFlowId);
}
