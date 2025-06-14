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

import integration.core.domain.IdentifierType;
import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.messaging.MessageFlow;
import integration.core.domain.messaging.OutboxEvent;
import integration.core.dto.OutboxEventDto;
import integration.core.dto.mapper.OutboxEventMapper;
import integration.core.exception.ComponentNotFoundException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.IntegrationException;
import integration.core.repository.ComponentRepository;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.nonretryable.OutboxEventNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.exception.retryable.OutboxEventProcessingException;
import integration.core.runtime.messaging.repository.MessageFlowRepository;
import integration.core.runtime.messaging.repository.OutboxEventRepository;
import integration.core.runtime.messaging.service.OutboxService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class OutboxServiceImpl implements OutboxService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxServiceImpl.class);
              
    @Autowired
    private OutboxEventRepository eventRepository;
    
    @Autowired
    private ComponentRepository componentRepository;
    
    @Autowired
    private MessageFlowRepository messageFlowRepository;

    
    @Override
    public void recordEvent(long messageFlowId, long componentId, long routeId, String owner) throws OutboxEventProcessingException, MessageFlowNotFoundException, ComponentNotFoundException {
        try {
            Optional<MessageFlow> messageFlowOptional = messageFlowRepository.findById(messageFlowId);
        
            // The message flow must exist.
            if (messageFlowOptional.isEmpty()) {
                throw new MessageFlowNotFoundException(messageFlowId);
            }      
            
            MessageFlow messageFlow = messageFlowOptional.get();
    
            OutboxEvent event = new OutboxEvent();
            event.setMessageFlow(messageFlow);
            
            Optional<IntegrationComponent> integrationComponent = componentRepository.findById(componentId);
            if (integrationComponent.isEmpty()) {
                throw new ComponentNotFoundException(componentId);
            }

            event.setComponent(integrationComponent.get());
            event.setRoute(integrationComponent.get().getRoute());
            event.setOwner(owner);
            
            eventRepository.save(event);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>otherIdentifiers = new ArrayList<>();
            otherIdentifiers.add(new ExceptionIdentifier(IdentifierType.COMPONENT_ID, componentId));
            throw new OutboxEventProcessingException("Database error while retrieving message flow", e).addOtherIdentifier(IdentifierType.COMPONENT_ID, componentId);
        }
    }

    
    @Override
    public List<OutboxEventDto> getEventsForComponent(long componentId, int numberToRead,List<Long>processedEventIds) throws OutboxEventProcessingException {
        try  {
            OutboxEventMapper mapper = new OutboxEventMapper();
            List<OutboxEventDto> eventDtos = new ArrayList<>();
               
            List<OutboxEvent> events = null;
            
            if (processedEventIds.isEmpty()) {
                events = eventRepository.getEventsForComponent(componentId, numberToRead);
            } else {
                events = eventRepository.getEventsForComponent(componentId, processedEventIds, numberToRead);
            }
    
            for (OutboxEvent event : events) {
                eventDtos.add(mapper.doMapping(event));
            }
            
            return eventDtos;
        } catch(DataAccessException e) {
            
            // There is no event id to put in the exception
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(IdentifierType.COMPONENT_ID, componentId));
            throw new OutboxEventProcessingException("Database error while retrieving events for component", e);
        } 
    }

    
    @Override
    public void deleteEvent(long eventId) throws OutboxEventProcessingException, OutboxEventNotFoundException {
        try {
            boolean eventExists = eventRepository.existsById(eventId);
            if (!eventExists) {
                throw new OutboxEventNotFoundException(eventId);
            }
            
            eventRepository.deleteById(eventId);
        } catch(DataAccessException e) {
            throw new OutboxEventProcessingException("Database error while deleting an event.", eventId ,e);
        }
    }

    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markEventForRetry(long eventId, IntegrationException theException) throws MessageFlowProcessingException, OutboxEventNotFoundException, OutboxEventProcessingException {
        try {
            Optional<OutboxEvent> eventOptional =  eventRepository.findById(eventId);
            if (eventOptional.isEmpty()) {
                throw new OutboxEventNotFoundException(eventId);
            }
            
            OutboxEvent event = eventOptional.get();
            
            int retryCount = event.getRetryCount();
            event.setRetryCount(++retryCount);
            event.setError(theException.toString());
            
            Calendar calendar = Calendar.getInstance();
            
            if (event.getRetryAfter() == null) {
                calendar.setTime(new Date());
            }

            int delaySeconds = 30 * retryCount;
            calendar.add(Calendar.SECOND, delaySeconds);
            
            event.setRetryAfter(calendar.getTime());
            
            eventRepository.save(event);  
        } catch(DataAccessException e) {
            throw new OutboxEventProcessingException("Database error while marking the event for retry", eventId, e);
        }
    }
}
