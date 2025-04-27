package integration.messaging.service;

public interface MetaDataService {
    
    String getMetaData(String key, long messageFlowStepId);
    
    void addMetaData(String key, String value, long messageFlowStepId);
}
