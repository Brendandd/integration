package integration.core.runtime.messaging.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import integration.core.domain.messaging.MessageFlowEvent;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowFiltered;
import integration.core.domain.messaging.MessageFlowGroup;
import integration.core.domain.messaging.MessageFlowProperty;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.MessageFlowEventDto;
import integration.core.dto.mapper.MessageFlowEventMapper;
import integration.core.dto.mapper.MessageFlowMapper;
import integration.core.exception.ComponentNotFoundException;
import integration.core.exception.ConfigurationException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.repository.ComponentRepository;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.splitter.SplitterException;
import integration.core.runtime.messaging.component.type.handler.transformation.TransformationException;
import integration.core.runtime.messaging.exception.EventProcessingException;
import integration.core.runtime.messaging.exception.MessageFlowException;
import integration.core.runtime.messaging.repository.MessageFlowEventRepository;
import integration.core.runtime.messaging.repository.MessageFlowRepository;
import integration.core.runtime.messaging.service.MessagingFlowService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MessagingFlowServiceImpl implements MessagingFlowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingFlowServiceImpl.class);

    @Autowired
    private MessageFlowRepository messageFlowRepository;
              
    @Autowired
    private MessageFlowEventRepository eventRepository;
    
    @Autowired
    private ComponentRepository componentRepository;

    
    @Override
    public void recordMessageFlowEvent(long messageFlowId, long componentId, MessageFlowEventType eventType) throws MessageFlowException, ConfigurationException {
        try {
            MessageFlow messageFlow = findMessageFlowById(messageFlowId);
    
            MessageFlowEvent event = new MessageFlowEvent();
            event.setMessageFlow(messageFlow);
            event.setType(eventType);
            
            Optional<IntegrationComponent> integrationComponent = componentRepository.findById(componentId);
            if (integrationComponent.isEmpty()) {
                throw new ComponentNotFoundException(componentId);
            }
            
            event.setComponent(integrationComponent.get());
            
            eventRepository.save(event);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>otherIdentifiers = new ArrayList<>();
            otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId));
            throw new MessageFlowException("Database error while retrieving message flow", messageFlowId, otherIdentifiers, e);
        }
    }

    
    /**
     * A helper method to retrieve a MessageFlowDto by id.
     * 
     * @param messageFlowId
     * @return
     * @throws MessageFlowException
     */
    private MessageFlow retrieveMandatoryMessageFlow(long messageFlowId) throws MessageFlowException {    
        try {
            Optional<MessageFlow> messageFlow = messageFlowRepository.findById(messageFlowId);
    
            // The message flow must exist.
            if (messageFlow.isEmpty()) {
                throw new MessageFlowException("Message flow not found", messageFlowId, new ArrayList<>(), false);
            }  
            
            return messageFlow.get();
        } catch(DataAccessException e) {
            throw new MessageFlowException("Message flow not found", messageFlowId, new ArrayList<>(), false);
        }
    }

    
    /**
     * Retrieves a message flowDto by id.
     */
    @Override
    public MessageFlowDto retrieveMessageFlow(long messageFlowId) throws MessageFlowException {    
        MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowId);
        
        MessageFlowMapper mapper = new MessageFlowMapper();
        
        return mapper.doMapping(messageFlow);
    }

    
    private MessageFlow findMessageFlowById(long messageFlowId) throws MessageFlowException {
        return retrieveMandatoryMessageFlow(messageFlowId);
    }

    
    @Override
    public List<MessageFlowEventDto> getEventsForComponent(long componentId, int numberToRead) throws MessageFlowException, EventProcessingException {
        try  {
            MessageFlowEventMapper mapper = new MessageFlowEventMapper();
            List<MessageFlowEventDto> eventDtos = new ArrayList<>();
    
            List<MessageFlowEvent> events = eventRepository.getEvents(componentId, numberToRead);
    
            for (MessageFlowEvent event : events) {
                eventDtos.add(mapper.doMapping(event));
            }
            
            return eventDtos;
        } catch(DataAccessException e) {
            
            // There is no event id to put in the exception
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId));
            throw new EventProcessingException("Database error while retrieving events for component", identifiers, e);
        } 
    }

    
    @Override
    public List<MessageFlowEventDto> getEvents(long componentId) throws MessageFlowException, EventProcessingException {
        try {
            MessageFlowEventMapper mapper = new MessageFlowEventMapper();
            List<MessageFlowEventDto> eventDtos = new ArrayList<>();
            
            List<MessageFlowEvent> events = eventRepository.getEvents(componentId);
    
            for (MessageFlowEvent event : events) {
                eventDtos.add(mapper.doMapping(event));
            }
            
            return eventDtos;
        } catch(DataAccessException e) {
            // There is no event id to put in the exception
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId));
            throw new EventProcessingException("Database error while retrieving events for component", identifiers, e);
        }
    }

    
    @Override
    public void deleteEvent(long eventId) throws EventProcessingException {
        try {
            eventRepository.deleteById(eventId);
        } catch(DataAccessException e) {
            throw new EventProcessingException("Database error while deleting an event.", eventId, new ArrayList<>(),e);
        }
    }

    
    @Override
    public MessageFlowDto recordMessageNotAccepted(long componentId, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowException, ConfigurationException {        
        return filterMessage(componentId, parentMessageFlowId, policyResult, action);
    }

    
    @Override
    public MessageFlowDto recordMessageNotForwarded(long componentId, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowException, ConfigurationException {        
        return filterMessage(componentId, parentMessageFlowId, policyResult, action);
    }

    
    private MessageFlowDto filterMessage(long componentId, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowException, ConfigurationException {  
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
            otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId));
            throw new MessageFlowException("Database error while filtering a message",parentMessageFlowId, otherIdentifiers, e);
        }
    }

    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MessageFlowDto recordTransformationError(long componentId, long parentMessageFlowId, TransformationException theException) throws MessageFlowException, ConfigurationException {  
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
            throw new MessageFlowException("Database error while transfotrming a message",parentMessageFlowId, otherIdentifiers, e);
        }
    }
    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MessageFlowDto recordSplitterError(long componentId, long parentMessageFlowId, SplitterException theException) throws MessageFlowException, ConfigurationException {  
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
            throw new MessageFlowException("Database error while splitting a message",parentMessageFlowId, otherIdentifiers, e);
        }
    }
    
    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MessageFlowDto recordFilterError(long componentId, long parentMessageFlowId, FilterException theException) throws MessageFlowException, ConfigurationException {  
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
            throw new MessageFlowException("Database error while filtering a message flow",parentMessageFlowId, otherIdentifiers, e);
        }
    }

    
    /**
     * Returns the specified message flow property.
     * 
     * @param key
     * @param messageFlowId
     * @return
     * @throws MessageFlowException 
     */
    @Override
    public String getMessageFlowProperty(String key, Long messageFlowId) throws MessageFlowException {
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
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID, messageFlowId));
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.PROPERTY_KEY, key));
            throw new MessageFlowException("Database error while getting a message flow property", messageFlowId, identifiers, e);
        }
    }

    
    @Override
    public MessageFlowDto recordMessageFlow(String messageContent, long componentId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowException, ConfigurationException {       
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponentId(componentId);
        request.setContentType(contentType);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }

    
    @Override
    public MessageFlowDto recordMessageFlow(String messageContent, long componentId, long parentMessageFlowId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowException, ConfigurationException {
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponentId(componentId);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setContentType(contentType);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }

    
    @Override
    public MessageFlowDto recordMessageFlow(long componentId, long parentMessageFlowId, MessageFlowActionType action) throws MessageFlowException, ConfigurationException {
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setComponentId(componentId);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }

    
    private MessageFlowDto recordMessageFlow(MessageFlowRequest request) throws MessageFlowException, ConfigurationException {
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
            List<ExceptionIdentifier>otherIdentifiers = new ArrayList<>();
            otherIdentifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, request.getComponentId()));
            throw new MessageFlowException("Database error while recording a message flow", savedStep != null ? savedStep.getId() : null, otherIdentifiers, e);
        }
    }

    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setEventFailed(long eventId) throws MessageFlowException, EventProcessingException {
        try {
            Optional<MessageFlowEvent> eventOptional =  eventRepository.findById(eventId);
            if (eventOptional.isEmpty()) {
                throw new EventProcessingException("Event not found", eventId, new ArrayList<>(), false);
            }
            
            MessageFlowEvent event = eventOptional.get();
            
            int retryCount = event.getRetryCount();
            event.setRetryCount(++retryCount);
            
            Calendar calendar = Calendar.getInstance();
            
            if (event.getRetryAfter() == null) {
                calendar.setTime(new Date());
            }

            int delaySeconds = 30 * retryCount;
            calendar.add(Calendar.SECOND, delaySeconds);
            
            event.setRetryAfter(calendar.getTime());
            
            eventRepository.save(event);  
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.EVENT_ID, eventId));
            throw new EventProcessingException("Database error while setting the event to failed", eventId, new ArrayList<>(), e);
        }
    }


    @Override
    public void updateAction(Long messageFlowId, MessageFlowActionType newAction) throws MessageFlowException {
        MessageFlow messageFlow = retrieveMandatoryMessageFlow(messageFlowId);
        
        messageFlow.setAction(newAction);
        
        messageFlowRepository.save(messageFlow);
        
    }
}
