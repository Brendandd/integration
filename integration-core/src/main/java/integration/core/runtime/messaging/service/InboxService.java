package integration.core.runtime.messaging.service;

import java.util.List;

import integration.core.dto.InboxEventDto;
import integration.core.exception.ComponentNotFoundException;
import integration.core.exception.IntegrationException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.nonretryable.OutboxEventNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.exception.retryable.OutboxEventProcessingException;

/**
 * Outbox services.
 * 
 * 
 */
public interface InboxService {
    
    /**
     * Records an inbox event when a message has been consumed from JMS.
     *  
     * @param messageFlowId
     * @param componentId
     * @param eventType
     * @throws MessageFlowProcessingException
     * @throws OutboxEventProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    void recordEvent(long messageFlowId, long componentId, long routeId, String owner, String jmsMessageId) throws MessageFlowProcessingException, OutboxEventProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    
    /**
     * 
     * 
     * @param messageFlowId
     * @param componentId
     * @param routeId
     * @param owner
     * @param jmsMessageId
     * @param eventType
     * @throws MessageFlowProcessingException
     * @throws OutboxEventProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    void recordEvent(long messageFlowId, long componentId, long routeId, String owner) throws MessageFlowProcessingException, OutboxEventProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    /**
     * Returns events for a component.
     * 
     * @param componentId
     * @param numberToRead
     * @return
     * @throws MessageFlowProcessingException
     * @throws OutboxEventProcessingException
     */
    List<InboxEventDto> getEventsForComponent(long componentId, int numberToRead,List<Long>processedEventIds) throws MessageFlowProcessingException, OutboxEventProcessingException;

    
    /**
     * Marks an event for retry.
     * 
     * @param eventId
     * @throws MessageFlowProcessingException
     * @throws OutboxEventProcessingException
     * @throws OutboxEventNotFoundException
     */
    void markEventForRetry(long eventId, IntegrationException theException) throws MessageFlowProcessingException, OutboxEventProcessingException, OutboxEventNotFoundException;
    
    
    /**
     * Deletes an event by id.
     * 
     * @param eventId
     * @throws OutboxEventProcessingException
     * @throws OutboxEventNotFoundException
     */
    void deleteEvent(long eventId) throws OutboxEventProcessingException, OutboxEventNotFoundException;
}
