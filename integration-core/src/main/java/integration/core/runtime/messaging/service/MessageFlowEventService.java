package integration.core.runtime.messaging.service;

import java.util.List;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowEventDto;
import integration.core.exception.ComponentNotFoundException;
import integration.core.exception.EventNotFoundException;
import integration.core.exception.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.EventProcessingException;
import integration.core.runtime.messaging.exception.MessageFlowProcessingException;

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
    void recordMessageFlowEvent(long messageFlowId, long componentId, MessageFlowEventType eventType) throws MessageFlowProcessingException, EventProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    
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
    List<MessageFlowEventDto> getEventsForComponent(long componentId, int numberToRead) throws MessageFlowProcessingException, EventProcessingException;

    
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
    List<MessageFlowEventDto> getEvents(long componentId) throws MessageFlowProcessingException, EventProcessingException;
    
    
    void setEventFailed(long eventId) throws MessageFlowProcessingException, EventProcessingException, EventNotFoundException;
    
    
    /**
     * Deletes an event by id.
     * 
     * @param eventId
     * @throws EventProcessingException 
     * @throws EventNotFoundException 
     * @throws RetryableException 
     */
    void deleteEvent(long eventId) throws EventProcessingException, EventNotFoundException;
}
