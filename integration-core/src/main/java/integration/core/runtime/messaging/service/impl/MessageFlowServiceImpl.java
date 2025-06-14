package integration.core.runtime.messaging.service.impl;

import java.util.Map;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.IdentifierType;
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
import integration.core.exception.IntegrationException;
import integration.core.repository.ComponentRepository;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.splitter.SplitterException;
import integration.core.runtime.messaging.component.type.handler.transformation.TransformationException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.repository.MessageFlowRepository;
import integration.core.runtime.messaging.service.MessageFlowPropertyService;
import integration.core.runtime.messaging.service.MessageFlowService;
import integration.core.runtime.messaging.service.OutboxService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MessageFlowServiceImpl implements MessageFlowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageFlowServiceImpl.class);

    @Autowired
    private MessageFlowRepository messageFlowRepository;
                  
    @Autowired
    private ComponentRepository componentRepository;
    
    @Autowired
    private OutboxService outboxService;
    
    @Autowired
    private MessageFlowPropertyService propertyService;
    
    
    /**
     * A helper method to retrieve a MessageFlowDto by id.
     * 
     * @param messageFlowId
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException 
     */
    private MessageFlow retrieveMandatoryMessageFlow(long messageFlowId, boolean includeMessage) throws MessageFlowProcessingException, MessageFlowNotFoundException {    
        try {
            Optional<MessageFlow> messageFlowOptional = messageFlowRepository.findById(messageFlowId);
    
            // The message flow must exist.
            if (messageFlowOptional.isEmpty()) {
                throw new MessageFlowNotFoundException(messageFlowId);
            }  
                       
            MessageFlow messageFlow = messageFlowOptional.get();
            
            if (includeMessage) {               
                Hibernate.initialize(messageFlow.getMessage().getContent());
            }
            
            return messageFlow;
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Message flow not found", messageFlowId, e);
        }
    }

    
    /**
     * Retrieves a message flowDto by id.
     * @throws MessageFlowNotFoundException 
     */
    @Override
    public MessageFlowDto retrieveMessageFlow(long messageFlowId, boolean includeMessage) throws MessageFlowProcessingException, MessageFlowNotFoundException {    
        MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowId, includeMessage);
        
        MessageFlowMapper mapper = new MessageFlowMapper(includeMessage);
        
        return mapper.doMapping(messageFlow);
    }

    
    @Override
    public MessageFlowDto recordMessageNotAccepted(long componentId, long parentMessageFlowId,MessageFlowPolicyResult policyResult) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {        
        return filterMessage(componentId, parentMessageFlowId, policyResult, MessageFlowActionType.MESSAGE_NOT_ACCEPTED);
    }

    
    @Override
    public MessageFlowDto recordMessageAccepted(long componentId, long parentMessageFlowId) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {  
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setComponentId(componentId);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setAction(MessageFlowActionType.MESSAGE_ACCEPTED);
        
        return recordMessageFlow(request);
    }

    
    @Override
    public MessageFlowDto recordMessageNotForwarded(long componentId, long parentMessageFlowId,MessageFlowPolicyResult policyResult) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {        
        return filterMessage(componentId, parentMessageFlowId, policyResult, MessageFlowActionType.MESSAGE_NOT_FORWARDED);
    }

    
    @Override
    public MessageFlowDto recordMessagePendingForwarding(long componentId, long parentMessageFlowId) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {        
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setComponentId(componentId);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setAction(MessageFlowActionType.MESSAGE_PENDING_FORWARDING);
        
        return recordMessageFlow(request);
    }

    
    private MessageFlowDto filterMessage(long componentId, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {  
        try {
            MessageFlowRequest request = new MessageFlowRequest();
            request.setComponentId(componentId);
            request.setParentMessageFlowId(parentMessageFlowId);
            request.setAction(action);
            
            MessageFlowDto messageFlowDto = recordMessageFlow(request);
            
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowDto.getId(), false);
                   
            // Create the filter object.
            MessageFlowFiltered filter = new MessageFlowFiltered();
            filter.setName(policyResult.getFilterName());
            filter.setReason(policyResult.getFilterReason());
            filter.setMessageFlow(messageFlow);
            
            messageFlow = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper(false);
            return mapper.doMapping(messageFlow);
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while filtering a message",parentMessageFlowId, e).addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
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
            
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowDto.getId(), false);
                   
            // Create the error object.
            MessageFlowError error = new MessageFlowError();
            error.setDetails(theException.toString());
            error.setMessageFlow(messageFlow);
            
            messageFlow = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper(false);
            return mapper.doMapping(messageFlow);
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while recording a transformation error",parentMessageFlowId, e).addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
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
            
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowDto.getId(), false);
                   
            // Create the error object.
            MessageFlowError error = new MessageFlowError();
            error.setDetails(theException.toString());
            error.setMessageFlow(messageFlow);
            
            messageFlow = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper(false);
            return mapper.doMapping(messageFlow);
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while recording a splitter error",parentMessageFlowId, e).addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
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
            
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowDto.getId(), false);
                   
            // Create the error object.
            MessageFlowError error = new MessageFlowError();
            error.setDetails(theException.toString());
            error.setMessageFlow(messageFlow);
            
            messageFlow = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper(false);
            return mapper.doMapping(messageFlow);
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while recording a filter error",parentMessageFlowId, e).addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
        }
    }

    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MessageFlowDto recordMessageFlowError(long componentId, long parentMessageFlowId, IntegrationException theException) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {  
        try {
            MessageFlowRequest request = new MessageFlowRequest();
            request.setComponentId(componentId);
            request.setParentMessageFlowId(parentMessageFlowId);
            request.setAction(MessageFlowActionType.PROCESSING_ERROR);
            
            MessageFlowDto messageFlowDto = recordMessageFlow(request);
            
            MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowDto.getId(), false);
                   
            // Create the error object.
            MessageFlowError error = new MessageFlowError();
            error.setDetails(theException.toString());
            error.setMessageFlow(messageFlow);
            
            messageFlow = messageFlowRepository.save(messageFlow);
            
            MessageFlowMapper mapper = new MessageFlowMapper(false);
            return mapper.doMapping(messageFlow);
        } catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while recording a message flow error",parentMessageFlowId, e).addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
        }
    }

    
    @Override
    public MessageFlowDto recordInitialMessageFlow(String messageContent, long componentId, ContentTypeEnum contentType, Map<String, Object>headers, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {       
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponentId(componentId);
        request.setContentType(contentType);
        request.setAction(action);
        request.setHeaders(headers);
        
        return recordMessageFlow(request);
    }

    
    @Override
    public MessageFlowDto recordNewContentMessageFlow(String messageContent, long componentId, long parentMessageFlowId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponentId(componentId);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setContentType(contentType);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }

    
    @Override
    public MessageFlowDto recordMessageFlowWithSameContent(long componentId, long parentMessageFlowId, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {
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
                parentMessageFlow = retrieveMandatoryMessageFlow(parentMessageFlowId, false);
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
            
            //TODO this is not ideal copying all the headers/properties to all message flows.  The plan is to only store the differences between the new message flow and the parents.
            if (request.getHeaders() != null) {
                for (Map.Entry<String, Object> entry : request.getHeaders().entrySet()) {
                    messageFlow.addOrUpdateProperty(entry.getKey(), entry.getValue());
                }
            }
    
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
            
            MessageFlowMapper mapper = new MessageFlowMapper(false);
            
            MessageFlowDto dto = mapper.doMapping(savedStep);
            
            return dto;
        }  catch(DataAccessException e) {
            throw new MessageFlowProcessingException("Database error while recording a message flow", request.getComponentId(),  e);
        }
    }

    
    @Override
    public void updatePendingForwardingToForwardedAction(Long messageFlowId) throws MessageFlowProcessingException, MessageFlowNotFoundException {
        MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowId, false);
        
        if (messageFlow.getAction() != MessageFlowActionType.MESSAGE_PENDING_FORWARDING) {
            throw new MessageFlowProcessingException("Message flow is not the correct action type.  It should have been: " + MessageFlowActionType.MESSAGE_PENDING_FORWARDING, messageFlow.getId());
        }
        
        messageFlow.setAction(MessageFlowActionType.MESSAGE_FORWARDED);
        
        messageFlowRepository.save(messageFlow);
        
    }
}
