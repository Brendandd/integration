package integration.core.service;

import java.util.List;
import java.util.Map;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.dto.MessageFlowEventDto;
import integration.core.dto.MessageFlowStepDto;
import integration.core.messaging.component.BaseMessagingComponent;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

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
     */
    void recordMessageFlowEvent(long messageFlowId, String componentPath, String owner, MessageFlowEventType eventType);
    

    /**
     * Records the message as not accepted (filtered).
     * 
     * @param messageConsumer
     * @param messageFlowStepId
     * @param contentType
     * @return
     */
    MessageFlowStepDto recordMessageNotAccepted(MessageConsumer component, long messageFlowStepId, MessageFlowPolicyResult policyResult, MessageFlowStepActionType action);
    
    
    /**
     * Records the message as not forwarded (filtered).
     * 
     * @param messageConsumer
     * @param messageFlowStepId
     * @param contentType
     * @return
     */
    MessageFlowStepDto recordMessageNotForwarded(MessageProducer component, long messageFlowStepId, MessageFlowPolicyResult policyResult, MessageFlowStepActionType action);

    /**
     * Records a message flow event without linking it to a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     */
    MessageFlowStepDto recordMessageFlowStep(String messageContent, BaseMessagingComponent component, String contentType,Map<String,String>metaData, MessageFlowStepActionType action);
    
    
    /**
     * Records a message flow event without linking it to a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     */
    MessageFlowStepDto recordMessageFlowStep(String messageContent, BaseMessagingComponent component, long parentMessageFlowId, String contentType, Map<String,String>metaData, MessageFlowStepActionType action);

    
    /**
     * Records a message flow event from a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     */
    MessageFlowStepDto recordMessageFlowStep(BaseMessagingComponent component, long parentMessageFlowStepId, Map<String,String>metaData, MessageFlowStepActionType action);
    
    

    
    /**
     * Retrieves a message flow.
     * 
     * @param messageFlowId
     * @return
     */
    MessageFlowStepDto retrieveMessageFlow(long messageFlowId);

    
    /**
     * Gets a list of matching events.
     * 
     * @param identifier
     * @param numberToRead
     * @param type
     * @return
     */
    List<MessageFlowEventDto> getEventsForComponent(String owner, int numberToRead, String componentPath);
    
    
    /**
     * Gets a list of matching events.
     * 
     * @param identifier
     * @param numberToRead
     * @param type
     * @return
     */
    List<MessageFlowEventDto> getEvents(String owner, String componentPath);
    
    
    void setEventFailed(long eventId);
    
    
    /**
     * Deletes an event by id.
     * 
     * @param eventId
     */
    void deleteEvent(long eventId);
    
    
    /**
     * Returns meta data from the message associated with the current message flow step.
     * 
     * @param key
     * @param messageFlowStepId
     * @return
     */
    String retrieveMessageMetaData(String key, Long messageFlowStepId);
}
