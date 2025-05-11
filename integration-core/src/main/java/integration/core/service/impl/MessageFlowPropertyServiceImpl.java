package integration.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.messaging.MessageFlow;
import integration.core.domain.messaging.MessageFlowProperty;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.messaging.MessageFlowException;
import integration.core.repository.MessageFlowRepository;
import integration.core.service.MessageFlowPropertyService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MessageFlowPropertyServiceImpl implements MessageFlowPropertyService {   
    
    @Autowired
    private MessageFlowRepository messageFlowRepository;

    
    @Override
    public String getPropertyValue(String key, long messageFlowId) throws MessageFlowException {
        try {
            Optional<MessageFlow>messageFlowOptional = messageFlowRepository.findById(messageFlowId);
            
            MessageFlow messageFlow = messageFlowOptional.get();
            MessageFlowProperty property = messageFlow.getProperty(key);
            
            if (property != null) {
                return property.getValue();
            }
            
            return null;
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>otherIdentifiers = new ArrayList<>();
            otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.PROPERTY_KEY, key));
            throw new MessageFlowException("Database error while getting a message flow property", messageFlowId, otherIdentifiers, e);
        }
    }

    
    @Override
    public void addProperty(String key, String value, long messageFlowId) throws MessageFlowException {   
        try {
            Optional<MessageFlow>messageFlowOptional = messageFlowRepository.findById(messageFlowId);
            
            MessageFlow messageFlow = messageFlowOptional.get();
            messageFlow.addOrUpdateProperty(key, value);
            
            messageFlowRepository.save(messageFlow); 
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>otherIdentifiers = new ArrayList<>();
            otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.PROPERTY_KEY, key));
            throw new MessageFlowException("Database error while saving a message flow property",messageFlowId, otherIdentifiers, e);
        }
    }
}