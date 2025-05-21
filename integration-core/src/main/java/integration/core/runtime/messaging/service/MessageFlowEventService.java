package integration.core.runtime.messaging.service;

import java.util.List;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowEventDto;
import integration.core.exception.ComponentNotFoundException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowEventNotFoundException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowEventProcessingException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowServiceProcessingException;

/**
 * Service to process message flow events.
 */
/**
 * 
 */
public interface MessageFlowEventService {
    
    /**
     * Records a message flow event.
     *  
     * @param messageFlow
     * @throws EventProcessingException 
     * @throws MessageFlowNotFoundException 
     * @throws ComponentNotFoundException 
     * @throws RetryableException 
     */
    void recordMessageFlowEvent(long messageFlowId, long componentId, MessageFlowEventType eventType) throws MessageFlowServiceProcessingException, MessageFlowEventProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    
    /**
     * Gets a list of matching events.
     * 
     * @param identifier
     * @param numberToRead
     * @param type
     * @return
     * @throws EventProcessingException 
     * @throws RetryableException 
     */
    List<MessageFlowEventDto> getEventsForComponent(long componentId, int numberToRead) throws MessageFlowServiceProcessingException, MessageFlowEventProcessingException;

    
    /**
     * Gets a list of matching events.
     * 
     * @param identifier
     * @param numberToRead
     * @param type
     * @return
     * @throws EventProcessingException 
     * @throws RetryableException 
     */
    List<MessageFlowEventDto> getEvents(long componentId) throws MessageFlowServiceProcessingException, MessageFlowEventProcessingException;
    
    
    void setEventFailed(long eventId) throws MessageFlowServiceProcessingException, MessageFlowEventProcessingException, MessageFlowEventNotFoundException;
    
    
    /**
     * Deletes an event by id.
     * 
     * @param eventId
     * @throws EventProcessingException 
     * @throws MessageFlowEventNotFoundException 
     * @throws RetryableException 
     */
    void deleteEvent(long eventId) throws MessageFlowEventProcessingException, MessageFlowEventNotFoundException;
}
