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

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.messaging.MessageFlow;
import integration.core.domain.messaging.MessageFlowEvent;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowEventDto;
import integration.core.dto.mapper.MessageFlowEventMapper;
import integration.core.exception.ComponentNotFoundException;
import integration.core.exception.EventNotFoundException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.exception.MessageFlowNotFoundException;
import integration.core.repository.ComponentRepository;
import integration.core.runtime.messaging.exception.EventProcessingException;
import integration.core.runtime.messaging.exception.MessageFlowProcessingException;
import integration.core.runtime.messaging.repository.MessageFlowEventRepository;
import integration.core.runtime.messaging.repository.MessageFlowRepository;
import integration.core.runtime.messaging.service.MessageFlowEventService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class EventServiceImpl implements MessageFlowEventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingFlowServiceImpl.class);
              
    @Autowired
    private MessageFlowEventRepository eventRepository;
    
    @Autowired
    private ComponentRepository componentRepository;
    
    @Autowired
    private MessageFlowRepository messageFlowRepository;

    
    @Override
    public void recordMessageFlowEvent(long messageFlowId, long componentId, MessageFlowEventType eventType) throws EventProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {
        try {
            Optional<MessageFlow> messageFlowOptional = messageFlowRepository.findById(messageFlowId);
        
            // The message flow must exist.
            if (messageFlowOptional.isEmpty()) {
                throw new MessageFlowNotFoundException(messageFlowId);
            }             
    
            MessageFlowEvent event = new MessageFlowEvent();
            event.setMessageFlow(messageFlowOptional.get());
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
            throw new EventProcessingException("Database error while retrieving message flow", messageFlowId, otherIdentifiers, e);
        }
    }

    
    @Override
    public List<MessageFlowEventDto> getEventsForComponent(long componentId, int numberToRead) throws MessageFlowProcessingException, EventProcessingException {
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
    public List<MessageFlowEventDto> getEvents(long componentId) throws MessageFlowProcessingException, EventProcessingException {
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
    public void deleteEvent(long eventId) throws EventProcessingException, EventNotFoundException {
        try {
            boolean eventExists = eventRepository.existsById(eventId);
            if (!eventExists) {
                throw new EventNotFoundException(eventId);
            }
            
            eventRepository.deleteById(eventId);
        } catch(DataAccessException e) {
            throw new EventProcessingException("Database error while deleting an event.", eventId, new ArrayList<>(),e);
        }
    }

    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setEventFailed(long eventId) throws MessageFlowProcessingException, EventProcessingException, EventNotFoundException {
        try {
            Optional<MessageFlowEvent> eventOptional =  eventRepository.findById(eventId);
            if (eventOptional.isEmpty()) {
                throw new EventNotFoundException(eventId);
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
}
