package integration.core.runtime.messaging.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.IdentifierType;
import integration.core.domain.messaging.MessageFlow;
import integration.core.domain.messaging.MessageFlowProperty;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.repository.MessageFlowRepository;
import integration.core.runtime.messaging.service.MessageFlowPropertyService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MessageFlowPropertyServiceImpl implements MessageFlowPropertyService {   
    
    @Autowired
    private MessageFlowRepository messageFlowRepository;

    
    @Override
    public Object getPropertyValue(String key, long messageFlowId) throws MessageFlowProcessingException {
        try {
            Optional<MessageFlow>messageFlowOptional = messageFlowRepository.findById(messageFlowId);
            
            MessageFlow messageFlow = messageFlowOptional.get();
            MessageFlowProperty property = messageFlow.getProperty(key);
            
            if (property != null) {
                return property.getValue();
            }
            
            return null;
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while getting a message flow property", messageFlowId, e).addOtherIdentifier(IdentifierType.PROPERTY_KEY, key);
        }
    }

    
    @Override
    public void addProperty(String key, Object value, long messageFlowId) throws MessageFlowProcessingException {   
        try {
            Optional<MessageFlow>messageFlowOptional = messageFlowRepository.findById(messageFlowId);
            
            MessageFlow messageFlow = messageFlowOptional.get();
            messageFlow.addOrUpdateProperty(key, value);
            
            messageFlowRepository.save(messageFlow); 
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while saving a message flow property",messageFlowId, e).addOtherIdentifier(IdentifierType.PROPERTY_KEY, key);
        }
    }
}