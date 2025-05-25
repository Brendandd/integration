package integration.core.runtime.messaging.service;

import java.util.List;

import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.OutboxEventDto;
import integration.core.exception.ComponentNotFoundException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.nonretryable.OutboxEventNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.exception.retryable.OutboxEventProcessingException;

/**
 * Outbox services.
 * 
 * 
 */
public interface OutboxService {
    
    /**
     * Records an outbox event.
     *  
     * @param messageFlowId
     * @param componentId
     * @param eventType
     * @throws MessageFlowProcessingException
     * @throws OutboxEventProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    void recordEvent(long messageFlowId, long componentId, OutboxEventType eventType) throws MessageFlowProcessingException, OutboxEventProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    
    /**
     * Returns events for a component.
     * 
     * @param componentId
     * @param numberToRead
     * @return
     * @throws MessageFlowProcessingException
     * @throws OutboxEventProcessingException
     */
    List<OutboxEventDto> getEventsForComponent(long componentId, int numberToRead) throws MessageFlowProcessingException, OutboxEventProcessingException;

    
    /**
     * Returns events for a component.
     * 
     * @param componentId
     * @return
     * @throws MessageFlowProcessingException
     * @throws OutboxEventProcessingException
     */
    List<OutboxEventDto> getEvents(long componentId) throws MessageFlowProcessingException, OutboxEventProcessingException;
    
    
    /**
     * Marks an event for retry.
     * 
     * @param eventId
     * @throws MessageFlowProcessingException
     * @throws OutboxEventProcessingException
     * @throws OutboxEventNotFoundException
     */
    void markEventForRetry(long eventId) throws MessageFlowProcessingException, OutboxEventProcessingException, OutboxEventNotFoundException;
    
    
    /**
     * Deletes an event by id.
     * 
     * @param eventId
     * @throws OutboxEventProcessingException
     * @throws OutboxEventNotFoundException
     */
    void deleteEvent(long eventId) throws OutboxEventProcessingException, OutboxEventNotFoundException;
}
