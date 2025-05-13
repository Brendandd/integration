package integration.core.runtime.messaging.service;

import java.util.List;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.MessageFlowEventDto;
import integration.core.exception.ConfigurationException;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.splitter.SplitterException;
import integration.core.runtime.messaging.component.type.handler.transformation.TransformationException;
import integration.core.runtime.messaging.exception.EventProcessingException;
import integration.core.runtime.messaging.exception.MessageFlowException;

/**
 * Service to store messages/message flows.
 */
/**
 * 
 */
public interface MessagingFlowService {
    
    /**
     * Records a message flow event.
     *  
     * @param messageFlow
     * @throws ConfigurationException 
     * @throws RetryableException 
     */
    void recordMessageFlowEvent(long messageFlowId, long componentId, MessageFlowEventType eventType) throws MessageFlowException, ConfigurationException;
    

    /**
     * Records the message as not accepted (filtered).
     * 
     * @param messageConsumer
     * @param messageFlowId
     * @param contentType
     * @return
     * @throws ConfigurationException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageNotAccepted(long componentId, long messageFlowId, MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowException, ConfigurationException;
    
    
    /**
     * Records the message as not forwarded (filtered).
     * 
     * @param messageConsumer
     * @param messageFlowId
     * @param contentType
     * @return
     * @throws ConfigurationException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageNotForwarded(long componentId, long messageFlowId, MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowException, ConfigurationException;

    /**
     * Records a message flow event without linking it to a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     * @throws ConfigurationException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageFlow(String messageContent, long componentId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowException, ConfigurationException;
    
    
    /**
     * Records a message flow event without linking it to a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     * @throws ConfigurationException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageFlow(String messageContent, long componentId, long parentMessageFlowId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowException, ConfigurationException;

    
    /**
     * Records a message flow event from a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     * @throws ConfigurationException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageFlow(long componentId, long parentMessageFlowId, MessageFlowActionType action) throws MessageFlowException, ConfigurationException;
    
    

    
    /**
     * Retrieves a message flow.
     * 
     * @param messageFlowId
     * @return
     * @throws RetryableException 
     */
    MessageFlowDto retrieveMessageFlow(long messageFlowId) throws MessageFlowException;

    
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
    List<MessageFlowEventDto> getEventsForComponent(long componentId, int numberToRead) throws MessageFlowException, EventProcessingException;

    
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
    List<MessageFlowEventDto> getEvents(long componentId) throws MessageFlowException, EventProcessingException;
    
    
    void setEventFailed(long eventId) throws MessageFlowException, EventProcessingException;
    
    
    /**
     * Deletes an event by id.
     * 
     * @param eventId
     * @throws EventProcessingException 
     * @throws RetryableException 
     */
    void deleteEvent(long eventId) throws EventProcessingException;
    
    
    /**
     * Returns a message flow property.
     * 
     * @param key
     * @param messageFlowId
     * @return
     * @throws RetryableException 
     */
    String getMessageFlowProperty(String key, Long messageFlowId) throws MessageFlowException;
    
    
    /**
     * Records a transformation failed message flow step.
     * 
     * @param componentId
     * @param parentMessageFlowId
     * @return
     * @throws MessageFlowException
     * @throws ConfigurationException
     */
    public MessageFlowDto recordTransformationError(long componentId, long messageFlowId, TransformationException theException) throws MessageFlowException, ConfigurationException;
    
    
    /**
     * Records a filter failed message flow step.
     * 
     * @param componentId
     * @param parentMessageFlowId
     * @return
     * @throws MessageFlowException
     * @throws ConfigurationException
     */
    public MessageFlowDto recordFilterError(long componentId, long messageFlowId, FilterException theException) throws MessageFlowException, ConfigurationException;
    
    
    /**
     * Records a splitter failed message flow step.
     * 
     * @param componentId
     * @param parentMessageFlowId
     * @return
     * @throws MessageFlowException
     * @throws ConfigurationException
     */
    public MessageFlowDto recordSplitterError(long componentId, long messageFlowId, SplitterException theException) throws MessageFlowException, ConfigurationException;


    /**
     * Updates the action of a message flow.
     * 
     * @param messageFlowId
     * @param forwarded
     * @throws MessageFlowException
     */
    void updateAction(Long messageFlowId, MessageFlowActionType forwarded) throws MessageFlowException;
}
