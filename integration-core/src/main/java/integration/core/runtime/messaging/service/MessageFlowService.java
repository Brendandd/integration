package integration.core.runtime.messaging.service;

import java.util.Map;

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
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;

/**
 * Service to store messages/message flows.
 */
public interface MessageFlowService {
      
    /**
     * Records the message as not accepted (filtered).
     * 
     * @param componentId
     * @param messageFlowId
     * @param policyResult
     * @param action
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    MessageFlowDto recordMessageNotAccepted(long componentId, long messageFlowId, MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    /**
     * Records the message as not forwarded (filtered).
     * 
     * @param componentId
     * @param messageFlowId
     * @param policyResult
     * @param action
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    MessageFlowDto recordMessageNotForwarded(long componentId, long messageFlowId, MessageFlowPolicyResult policyResult, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    
    /**
     * Records the initial message flow.   
     * 
     * @param messageContent
     * @param componentId
     * @param contentType
     * @param headers
     * @param action
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    MessageFlowDto recordInitialMessageFlow(String messageContent, long componentId, ContentTypeEnum contentType, Map<String, Object> headers, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    /**
     * Records a message flow event linked to a parent message flow where the content has been changed.
     * 
     * @param messageContent
     * @param componentId
     * @param parentMessageFlowId
     * @param contentType
     * @param action
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    MessageFlowDto recordNewContentMessageFlow(String messageContent, long componentId, long parentMessageFlowId, ContentTypeEnum contentType, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    
    /**
     * Records a message flow event linked to a parent message flow where the content is the same.
     * 
     * @param componentId
     * @param parentMessageFlowId
     * @param action
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    MessageFlowDto recordMessageFlowWithSameContent(long componentId, long parentMessageFlowId, MessageFlowActionType action) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    
    /**
     * Retrieves a message flow.
     * 
     * @param messageFlowId
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     */
    MessageFlowDto retrieveMessageFlow(long messageFlowId) throws MessageFlowProcessingException, MessageFlowNotFoundException;

    
    /**
     * Records a transformation failed message flow step.
     * 
     * @param componentId
     * @param messageFlowId
     * @param theException
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    MessageFlowDto recordTransformationError(long componentId, long messageFlowId, TransformationException theException) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    /**
     * Records a filter failed message flow step.
     * 
     * @param componentId
     * @param messageFlowId
     * @param theException
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    MessageFlowDto recordFilterError(long componentId, long messageFlowId, FilterException theException) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;
    
    
    /**
     * Records a splitter failed message flow step.
     * 
     * @param componentId
     * @param messageFlowId
     * @param theException
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    MessageFlowDto recordSplitterError(long componentId, long messageFlowId, SplitterException theException) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;

    
    /**
     * Records a failed message flow.  
     * 
     * @param componentId
     * @param messageFlowId
     * @param theException
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     * @throws ComponentNotFoundException
     */
    MessageFlowDto recordMessageFlowError(long componentId, long messageFlowId, IntegrationException theException) throws MessageFlowProcessingException, MessageFlowNotFoundException, ComponentNotFoundException;


    /**
     * Updates the action of a message flow.
     * 
     * @param messageFlowId
     * @param forwarded
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     */
    void updateAction(Long messageFlowId, MessageFlowActionType forwarded) throws MessageFlowProcessingException, MessageFlowNotFoundException;
}
