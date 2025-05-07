package integration.core.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.messaging.MessageFlow;
import integration.core.domain.messaging.MessageFlowProperty;
import integration.core.repository.MessageFlowRepository;
import integration.core.service.MessageFlowPropertyService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MessageFlowPropertyServiceImpl implements MessageFlowPropertyService {   
    @Autowired
    private MessageFlowRepository messageFlowRepository;

    
    @Override
    public String getPropertyValue(String key, long messageFlowId) {
        Optional<MessageFlow>messageFlowOptional = messageFlowRepository.findById(messageFlowId);
        
        MessageFlow messageFlow = messageFlowOptional.get();
        MessageFlowProperty property = messageFlow.getProperty(key);
        
        if (property != null) {
            return property.getValue();
        }
        
        return null;
    }

    
    @Override
    public void addProperty(String key, String value, long messageFlowId) {       
        Optional<MessageFlow>messageFlowOptional = messageFlowRepository.findById(messageFlowId);
        
        MessageFlow messageFlow = messageFlowOptional.get();
        messageFlow.addOrUpdateProperty(key, value);
        
        messageFlowRepository.save(messageFlow); 
    }
}
