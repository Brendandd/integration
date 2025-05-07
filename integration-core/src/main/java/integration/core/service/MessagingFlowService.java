package integration.core.service;

import java.util.List;

import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.MessageFlowEventDto;
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
     * @param messageFlowId
     * @param contentType
     * @return
     */
    MessageFlowDto recordMessageNotAccepted(MessageConsumer component, long messageFlowId, MessageFlowPolicyResult policyResult, MessageFlowActionType action);
    
    
    /**
     * Records the message as not forwarded (filtered).
     * 
     * @param messageConsumer
     * @param messageFlowId
     * @param contentType
     * @return
     */
    MessageFlowDto recordMessageNotForwarded(MessageProducer component, long messageFlowId, MessageFlowPolicyResult policyResult, MessageFlowActionType action);

    /**
     * Records a message flow event without linking it to a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     */
    MessageFlowDto recordMessageFlow(String messageContent, BaseMessagingComponent component, String contentType, MessageFlowActionType action);
    
    
    /**
     * Records a message flow event without linking it to a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     */
    MessageFlowDto recordMessageFlow(String messageContent, BaseMessagingComponent component, long parentMessageFlowId, String contentType, MessageFlowActionType action);

    
    /**
     * Records a message flow event from a parent.
     * 
     * @param routeId
     * @param componentId
     * @param content
     */
    MessageFlowDto recordMessageFlow(BaseMessagingComponent component, long parentMessageFlowId, MessageFlowActionType action);
    
    

    
    /**
     * Retrieves a message flow.
     * 
     * @param messageFlowId
     * @return
     */
    MessageFlowDto retrieveMessageFlow(long messageFlowId);

    
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
     * Returns a message flow property.
     * 
     * @param key
     * @param messageFlowId
     * @return
     */
    String getMessageFlowProperty(String key, Long messageFlowId);
}
