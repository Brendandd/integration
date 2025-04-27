package integration.messaging.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.messaging.service.MetaDataService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MetaDataServiceImpl implements MetaDataService {
    
    @Override
    public String getMetaData(String key, long messageFlowStepId) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    @Override
    public void addMetaData(String key, String value, long messageFlowStepId) {
        // TODO Auto-generated method stub
        
    }  
}
