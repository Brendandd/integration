package integration.messaging.service;

import java.util.List;
import java.util.Map;

import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.dto.MessageFlowEventDto;
import integration.core.dto.MessageFlowStepDto;
import integration.messaging.ComponentIdentifier;
import integration.messaging.component.BaseMessagingComponent;
import integration.messaging.component.MessageConsumer;
import integration.messaging.component.adapter.BaseInboundAdapter;
import integration.messaging.component.connector.BaseInboundRouteConnector;
import integration.messaging.component.connector.BaseOutboundRouteConnector;

/**
 * Service to store messages/message flows.
 */
/**
 * 
 */
public interface MessagingFlowService {
   
    /**
     * Records the message received by a message consumer.  The consumers are internal to a single route.
     * 
     * @param receivingComponentIdentifier
     * @param parentComponentIdentifier
     * @return
     */
    MessageFlowStepDto recordMessageAccepted(MessageConsumer messageConsumer, long messageFlowStepId, String contentType);

    
    /**
     * Records the message received by an inbound route connector which was produced in another route.
     * 
     * @param inboundRouteConnector
     * @param messageFlowStepId
     * @param contentType
     * @return
     */
    MessageFlowStepDto recordInboundMessageProducedByOtherRoute(BaseInboundRouteConnector inboundRouteConnector, long messageFlowStepId, String contentType);

    
    /**
     * Records the message to be consumed by another route.
     * 
     * @param outboundRouteConnector
     * @param messageFlowStepId
     * @param contentType
     * @return
     */
    MessageFlowStepDto recordOutboundMessageToBeConsumedByOtherRoute(BaseOutboundRouteConnector outboundRouteConnector, long messageFlowStepId, String contentType,MessageFlowStepActionType action);

    
    /**
     * Records the message received by from an external source.  This only applied to inbound adapters.
     * 
     * @param message
     * @param componentRouteId
     * @param contentType
     * @return
     */
    MessageFlowStepDto recordMessageReceivedFromExternalSource(String messageContent, BaseInboundAdapter inboundAdapter, String contentType);
    
    
    /**
     * Records the message received by from an external source.  This only applied to inbound adapters.
     * 
     * @param messageContent
     * @param inboundAdapter
     * @param contentType
     * @param headers
     * @return
     */
    MessageFlowStepDto recordMessageReceivedFromExternalSource(String messageContent, BaseInboundAdapter inboundAdapter, String contentType, Map<String,String>metadata);

    
    /**
     * Records the message dispatched by a components outbound handler.
     * 
     * @param messageContent
     * @param messagingComponent
     * @param parentMessageFlowStepId
     * @param contentType
     * @return
     */
    MessageFlowStepDto recordOutboundMessageHandlerComplete(String messageContent, BaseMessagingComponent messagingComponent, long parentMessageFlowStepId, String contentType);

    
    /**
     * Records a message flow event.
     * 
     * @param messageFlow
     */
    void recordMessageFlowEvent(long messageFlowId, MessageFlowEventType eventType);
    
    
    /**
     * Records an ACK.
     * 
     * @param routeId
     * @param componentId
     * @param content
     */
    void recordAck(String ackContent, BaseInboundAdapter inboundAdapter, Long fromMessageFlowStepId, String contentType);
    
    
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
    List<MessageFlowEventDto> getEvents(ComponentIdentifier identifier, int numberToRead, MessageFlowEventType type);
    
    
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
