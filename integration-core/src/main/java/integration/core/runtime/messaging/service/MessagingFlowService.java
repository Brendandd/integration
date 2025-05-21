package integration.core.runtime.messaging.service;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.dto.MessageFlowDto;
import integration.core.exception.ComponentNotFoundException;
import integration.core.exception.IntegrationException;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.splitter.SplitterException;
import integration.core.runtime.messaging.component.type.handler.transformation.TransformationException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowServiceProcessingException;

/**
 * Service to store messages/message flows.
 */
/**
 * 
 */
public interface MessagingFlowService {
      
    /**
     * Records the message as not accepted (filtered).
     * 
     * @param messageConsumer
     * @param messageFlowId
     * @param contentType
     * @return
     * @throws MessageFlowNotFoundException 
     * @throws ComponentNotFoundException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageNotAccepted(long componentId, long messageFlowId, MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    /**
     * Records the message as not forwarded (filtered).
     * 
     * @param messageConsumer
     * @param messageFlowId
     * @param contentType
     * @return
     * @throws MessageFlowNotFoundException 
     * @throws ComponentNotFoundException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageNotForwarded(long componentId, long messageFlowId, MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    /**
     * Records a message flow event without linking it to a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     * @throws ComponentNotFoundException 
     * @throws MessageFlowNotFoundException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageFlow(String messageContent, long componentId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    /**
     * Records a message flow event without linking it to a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     * @throws ComponentNotFoundException 
     * @throws MessageFlowNotFoundException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageFlow(String messageContent, long componentId, long parentMessageFlowId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    
    /**
     * Records a message flow event from a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     * @throws ComponentNotFoundException 
     * @throws MessageFlowNotFoundException 
     * @throws RetryableException 
     */
    MessageFlowDto recordMessageFlow(long componentId, long parentMessageFlowId, MessageFlowActionType action) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    

    
    /**
     * Retrieves a message flow.
     * 
     * @param messageFlowId
     * @return
     * @throws MessageFlowNotFoundException 
     * @throws RetryableException 
     */
    MessageFlowDto retrieveMessageFlow(long messageFlowId) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException;

    /**
     * Returns a message flow property.
     * 
     * @param key
     * @param messageFlowId
     * @return
     * @throws MessageFlowNotFoundException 
     * @throws RetryableException 
     */
    String getMessageFlowProperty(String key, Long messageFlowId) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException;
    
    
    /**
     * Records a transformation failed message flow step.
     * 
     * @param componentId
     * @param parentMessageFlowId
     * @return
     * @throws MessageFlowServiceProcessingException
     * @throws MessageFlowNotFoundException 
     * @throws ComponentNotFoundException 
     */
    public MessageFlowDto recordTransformationError(long componentId, long messageFlowId, TransformationException theException) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    /**
     * Records a filter failed message flow step.
     * 
     * @param componentId
     * @param parentMessageFlowId
     * @return
     * @throws MessageFlowServiceProcessingException
     * @throws MessageFlowNotFoundException 
     * @throws ComponentNotFoundException 
     */
    public MessageFlowDto recordFilterError(long componentId, long messageFlowId, FilterException theException) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    /**
     * Records a splitter failed message flow step.
     * 
     * @param componentId
     * @param parentMessageFlowId
     * @return
     * @throws MessageFlowServiceProcessingException
     * @throws MessageFlowNotFoundException 
     * @throws ComponentNotFoundException 
     */
    public MessageFlowDto recordSplitterError(long componentId, long messageFlowId, SplitterException theException) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    
    /**
     * Records a failed message flow.  
     * 
     * @param componentId
     * @param parentMessageFlowId
     * @return
     * @throws MessageFlowServiceProcessingException
     * @throws MessageFlowNotFoundException 
     * @throws ComponentNotFoundException 
     */
    public MessageFlowDto recordMessageFlowError(long componentId, long messageFlowId, IntegrationException theException) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;


    /**
     * Updates the action of a message flow.
     * 
     * @param messageFlowId
     * @param forwarded
     * @throws MessageFlowServiceProcessingException
     * @throws MessageFlowNotFoundException 
     */
    void updateAction(Long messageFlowId, MessageFlowActionType forwarded) throws MessageFlowServiceProcessingException, MessageFlowNotFoundException;
}
