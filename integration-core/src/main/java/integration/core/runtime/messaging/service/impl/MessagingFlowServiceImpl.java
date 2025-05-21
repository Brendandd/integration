package integration.core.runtime.messaging.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.messaging.Message;
import integration.core.domain.messaging.MessageFlow;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowError;
import integration.core.domain.messaging.MessageFlowFiltered;
import integration.core.domain.messaging.MessageFlowGroup;
import integration.core.domain.messaging.MessageFlowProperty;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.mapper.MessageFlowMapper;
import integration.core.exception.ComponentNotFoundException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.repository.ComponentRepository;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.splitter.SplitterException;
import integration.core.runtime.messaging.component.type.handler.transformation.TransformationException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.repository.MessageFlowRepository;
import integration.core.runtime.messaging.service.MessagingFlowService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MessagingFlowServiceImpl implements MessagingFlowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingFlowServiceImpl.class);

    @Autowired
    private MessageFlowRepository messageFlowRepository;
                  
    @Autowired
    private ComponentRepository componentRepository;


    /**
     * A helper method to retrieve a MessageFlowDto by id.
     * 
     * @param messageFlowId
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException 
     */
    private MessageFlow retrieveMandatoryMessageFlow(long messageFlowId) throws MessageFlowProcessingException, MessageFlowNotFoundException {    
        try {
            Optional<MessageFlow> messageFlow = messageFlowRepository.findById(messageFlowId);
    
            // The message flow must exist.
            if (messageFlow.isEmpty()) {
                throw new MessageFlowNotFoundException(messageFlowId);
            }  
            
            return messageFlow.get();
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Message flow not found", messageFlowId, e);
        }
    }

    
    /**
     * Retrieves a message flowDto by id.
     * @throws MessageFlowNotFoundException 
     */
    @Override
    public MessageFlowDto retrieveMessageFlow(long messageFlowId) throws MessageFlowProcessingException, MessageFlowNotFoundException {    
        MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowId);
        
        MessageFlowMapper mapper = new MessageFlowMapper();
        
        return mapper.doMapping(messageFlow);
    }

    
    @Override
    public MessageFlowDto recordMessageNotAccepted(long componentId, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {        
        return filterMessage(componentId, parentMessageFlowId, policyResult, action);
    }

    
    @Override
    public MessageFlowDto recordMessageNotForwarded(long componentId, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {        
        return filterMessage(componentId, parentMessageFlowId, policyResult, action);
    }

    
    private MessageFlowDto filterMessage(long componentId, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {  
        try {
            MessageFlowRequest request = new MessageFlowRequest();
            request.setComponentId(componentId);
            request.setParentMessageFlowId(parentMessageFlowId);
            request.setAction(action);
            
            MessageFlowDto messageFlowDto = recordMessageFlow(request);
            
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowDto.getId());
                   
            // Create the filter object.
            MessageFlowFiltered filter = new MessageFlowFiltered();
            filter.setName(policyResult.getFilterName());
            filter.setReason(policyResult.getFilterReason());
            filter.setMessageFlow(messageFlow);
            
            messageFlow = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper();
            return mapper.doMapping(messageFlow);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>otherIdentifiers = new ArrayList<>();
            throw new MessageFlowProcessingException("Database error while filtering a message",parentMessageFlowId, e).addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
        }
    }

    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MessageFlowDto recordTransformationError(long componentId, long parentMessageFlowId, TransformationException theException) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {  
        try {
            MessageFlowRequest request = new MessageFlowRequest();
            request.setComponentId(componentId);
            request.setParentMessageFlowId(parentMessageFlowId);
            request.setAction(MessageFlowActionType.TRANSFORMATION_ERROR);
            
            MessageFlowDto messageFlowDto = recordMessageFlow(request);
            
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowDto.getId());
                   
            // Create the error object.
            MessageFlowError error = new MessageFlowError();
            error.setDetails(theException.toString());
            error.setMessageFlow(messageFlow);
            
            messageFlow = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper();
            return mapper.doMapping(messageFlow);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>otherIdentifiers = new ArrayList<>();
            otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId));
            throw new MessageFlowProcessingException("Database error while transfotrming a message",parentMessageFlowId, e).addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
        }
    }
    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MessageFlowDto recordSplitterError(long componentId, long parentMessageFlowId, SplitterException theException) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {  
        try {
            MessageFlowRequest request = new MessageFlowRequest();
            request.setComponentId(componentId);
            request.setParentMessageFlowId(parentMessageFlowId);
            request.setAction(MessageFlowActionType.SPLITTER_ERROR);
            
            MessageFlowDto messageFlowDto = recordMessageFlow(request);
            
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowDto.getId());
                   
            // Create the error object.
            MessageFlowError error = new MessageFlowError();
            error.setDetails(theException.toString());
            error.setMessageFlow(messageFlow);
            
            messageFlow = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper();
            return mapper.doMapping(messageFlow);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>otherIdentifiers = new ArrayList<>();
            otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId));
            throw new MessageFlowProcessingException("Database error while splitting a message",parentMessageFlowId, e).addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
        }
    }
    
    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MessageFlowDto recordFilterError(long componentId, long parentMessageFlowId, FilterException theException) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {  
        try {
            MessageFlowRequest request = new MessageFlowRequest();
            request.setComponentId(componentId);
            request.setParentMessageFlowId(parentMessageFlowId);
            request.setAction(MessageFlowActionType.FILTER_ERROR);
            
            MessageFlowDto messageFlowDto = recordMessageFlow(request);
            
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowDto.getId());
                   
            // Create the error object.
            MessageFlowError error = new MessageFlowError();
            error.setDetails(theException.toString());
            error.setMessageFlow(messageFlow);
            
            messageFlow = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper();
            return mapper.doMapping(messageFlow);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>otherIdentifiers = new ArrayList<>();
            otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId));
            throw new MessageFlowProcessingException("Database error while filtering a message flow",parentMessageFlowId, e).addOtherIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId);
        }
    }

    
    /**
     * Returns the specified message flow property.
     * 
     * @param key
     * @param messageFlowId
     * @return
     * @throws MessageFlowNotFoundException 
     * @throws MessageFlowProcessingException 
     */
    @Override
    public String getMessageFlowProperty(String key, Long messageFlowId) throws MessageFlowProcessingException, MessageFlowNotFoundException {
        try {
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowId);
            
            List<MessageFlowProperty>properties = messageFlow.getProperties();
                   
            for (MessageFlowProperty property : properties) {
                if (property.getKey().equals(key)) {                
                    return property.getValue();
                }
            }
            
            return null;
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while getting a message flow property", messageFlowId, e).addOtherIdentifier(ExceptionIdentifierType.PROPERTY_KEY, key);
        }
    }

    
    @Override
    public MessageFlowDto recordMessageFlow(String messageContent, long componentId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {       
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponentId(componentId);
        request.setContentType(contentType);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }

    
    @Override
    public MessageFlowDto recordMessageFlow(String messageContent, long componentId, long parentMessageFlowId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponentId(componentId);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setContentType(contentType);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }

    
    @Override
    public MessageFlowDto recordMessageFlow(long componentId, long parentMessageFlowId, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setComponentId(componentId);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }

    
    private MessageFlowDto recordMessageFlow(MessageFlowRequest request) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {
        MessageFlow savedStep = null;
        
        try {
            String messageContent = request.getMessageContent();
            long componentId = request.getComponentId();
            Long parentMessageFlowId = request.getParentMessageFlowId();
            ContentTypeEnum contentType = request.getContentType();
            MessageFlowActionType action = request.getAction();
            
            
            // Get the parent message flow if an id was supplied.
            MessageFlow parentMessageFlow = null;
            
            if (parentMessageFlowId != null) {
                parentMessageFlow = retrieveMandatoryMessageFlow(parentMessageFlowId);
            }
    
            // If the message was not supplied then create it from the parent message flow id if that was supplied.
            Message message = null;
            
            if (parentMessageFlow != null) {
                if (messageContent == null) {
                    message = parentMessageFlow.getMessage();                 
                } else {
                    // If message content was supplied then compare against the parent message.  If different then create a new message.
                    if (!messageContent.equals(parentMessageFlow.getMessage().getContent())) {
                        message = new Message(messageContent, contentType);
                    } 
                }
            } else {
                // There is no parent so store the original message.
                message = new Message(messageContent, contentType);
            }
            
            Optional<IntegrationComponent> integrationComponent = componentRepository.findById(componentId);
            if (integrationComponent.isEmpty()) {
                throw new ComponentNotFoundException(componentId);
            }

            MessageFlow messageFlow = new MessageFlow();
            messageFlow.setComponent(integrationComponent.get());
            messageFlow.setMessage(message);
            messageFlow.setAction(action);
    
            // Associate the new message flow with its parent.
            if (parentMessageFlow != null) {
                messageFlow.setParentMessageFlow(parentMessageFlow);
                
                // Copy all properties
                for (MessageFlowProperty property : parentMessageFlow.getProperties()) {
                    messageFlow.addOrUpdateProperty(property.getKey(), property.getValue());
                }
            }
    
            MessageFlowGroup group = null;
            
            // If the parent is null this is the original message so a new group needs creating.
            if (parentMessageFlow == null) {
                group = new MessageFlowGroup();
            } else {
                group = parentMessageFlow.getGroup();
            }
            
            group.addMessageFlow(messageFlow);
            
            savedStep = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper();
            return mapper.doMapping(savedStep);
        }  catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while recording a message flow", request.getComponentId(),  e);
        }
    }

    
    @Override
    public void updateAction(Long messageFlowId, MessageFlowActionType newAction) throws MessageFlowProcessingException, MessageFlowNotFoundException {
        MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowId);
        
        messageFlow.setAction(newAction);
        
        messageFlowRepository.save(messageFlow);
        
    }
}
