package integration.messaging.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.ComponentRoute;
import integration.core.domain.messaging.Message;
import integration.core.domain.messaging.MessageFlowEvent;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowGroup;
import integration.core.domain.messaging.MessageFlowStep;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.domain.messaging.MessageMetaData;
import integration.core.dto.MessageFlowEventDto;
import integration.core.dto.MessageFlowStepDto;
import integration.core.dto.mapper.MessageFlowEventMapper;
import integration.core.dto.mapper.MessageFlowStepMapper;
import integration.core.exception.ConfigurationException;
import integration.core.repository.ComponentRouteRepository;
import integration.core.repository.MessageFlowEventRepository;
import integration.core.repository.MessageFlowRepository;
import integration.core.repository.MessageFlowStepRepository;
import integration.core.repository.MessageRepository;
import integration.messaging.ComponentIdentifier;
import integration.messaging.component.BaseMessagingComponent;
import integration.messaging.component.MessageConsumer;
import integration.messaging.component.adapter.BaseInboundAdapter;
import integration.messaging.component.connector.BaseInboundRouteConnector;
import integration.messaging.component.connector.BaseOutboundRouteConnector;
import integration.messaging.service.MessagingFlowService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MessagingFlowServiceImpl implements MessagingFlowService {
    private static final Logger logger = LoggerFactory.getLogger(MessagingFlowServiceImpl.class);

    @Autowired
    private MessageFlowStepRepository messageFlowStepRepository;
    
    @Autowired
    private MessageFlowRepository messageFlowRepository;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ComponentRouteRepository componentRouteRepository;
    
    @Autowired
    private MessageFlowEventRepository eventRepository;

    
    @Override
    public void recordMessageFlowEvent(long messageFlowId, MessageFlowEventType eventType) {
        MessageFlowStep messageFlow = findMessageFlowById(messageFlowId);

        MessageFlowEvent event = new MessageFlowEvent();
        event.setMessageFlow(messageFlow);
        event.setType(eventType);
        eventRepository.save(event);
    }

    
    /**
     * Creates an returns a message flow object with an optional from message flow.
     * 
     * @param route
     * @param component
     * @param message
     * @param from
     * @return
     */
    private MessageFlowStep createMessageFlowStep(Message message, BaseMessagingComponent component, Long parentMessageFlowStepId, Map<String,String>metaData, MessageFlowStepActionType action) {
        if (component == null) {
            throw new RuntimeException("Component must not be null");
        }
        
        if (message == null && parentMessageFlowStepId == null) {
            throw new RuntimeException("Either message or parentMessageFlowId must not be null");
        }
        
        
        // Get the parent message flow step if an id was supplied.
        MessageFlowStep parentMessageFlowStep = null;
        if (parentMessageFlowStepId != null) {
            Optional<MessageFlowStep> parentFlowStepOptional = messageFlowRepository.findById(parentMessageFlowStepId);
            parentMessageFlowStep = parentFlowStepOptional.get();
        }
        
        
        // If the message was not supplied then create it from the parent message flow id if that was supplied.
        if (message == null && parentMessageFlowStepId != null) {           
            message = parentMessageFlowStep.getMessage();
            message.setMetaData(parentMessageFlowStep.getMessage().getMetaData());  // Copy the metadata to the new message.
        }

        
        Optional<ComponentRoute> componentRouteOptional = componentRouteRepository.findById(component.getIdentifier().getComponentRouteId());

        MessageFlowStep messageFlowStep = new MessageFlowStep();
        messageFlowStep.setComponentRoute(componentRouteOptional.get());
        messageFlowStep.setMessage(message);
        messageFlowStep.setAction(action);

        // Associate the new message flow step with its parent.
        if (parentMessageFlowStep != null) {
            messageFlowStep.setFromMessageFlowStep(parentMessageFlowStep);
        }

        MessageFlowGroup group = null;
        
        // If the parent is null this is the original message so a new group needs creating.
        if (parentMessageFlowStep == null) {
            group = new MessageFlowGroup();
        } else {
            group = parentMessageFlowStep.getMessageFlowGroup();
        }
        
        group.addMessageFlowStep(messageFlowStep);
        
        // Add or replace metadata stored against the message.
        if (metaData != null) {
            for (Map.Entry<String, String> entry : metaData.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                message.addMetaData(key, value);
            }
        }
        
        return messageFlowStepRepository.save(messageFlowStep);
    }

    
    /**
     * Retrieves a message flowDto by id.
     */
    @Override
    public MessageFlowStepDto retrieveMessageFlow(long messageFlowId) {       
        Optional<MessageFlowStep> messageFlow = messageFlowStepRepository.findById(messageFlowId);

        // The message flow must exist.
        if (messageFlow.isEmpty()) {
            throw new ConfigurationException("Message flow not found. Id: " + messageFlowId);
        }

        MessageFlowStepMapper mapper = new MessageFlowStepMapper();

        return mapper.doMapping(messageFlow.get());
    }
    
    
    private MessageFlowStep findMessageFlowById(long messageFlowId) {
        Optional<MessageFlowStep> messageFlow = messageFlowStepRepository.findById(messageFlowId);

        // The message flow must exist.
        if (messageFlow.isEmpty()) {
            throw new ConfigurationException("Message flow not found. Id: " + messageFlowId);
        }

        return messageFlow.get();
    }

    
    @Override
    public List<MessageFlowEventDto> getEvents(ComponentIdentifier identifier, int numberToRead, MessageFlowEventType type) {
        MessageFlowEventMapper mapper = new MessageFlowEventMapper();

        List<MessageFlowEventDto> eventDtos = new ArrayList<>();

        List<MessageFlowEvent> events = eventRepository.getEvents(identifier.getComponentRouteId(), type, numberToRead);

        for (MessageFlowEvent event : events) {
            eventDtos.add(mapper.doMapping(event));
        }

        return eventDtos;
    }

    
    @Override
    public void deleteEvent(long eventId) {
        eventRepository.deleteById(eventId);
    }

    
    /**
     * Returns the message object for the supplied message flow step id.
     * 
     * @param messageFlowStepId
     * @return
     */
    private Message retrieveMessage(long messageFlowStepId) {
        Optional<MessageFlowStep> messageFlowStepOptional = messageFlowRepository.findById(messageFlowStepId);
        
        if (messageFlowStepOptional.isEmpty()) {
            throw new RuntimeException("Message flow step not found: " + messageFlowStepId);
        }
        
        return messageFlowStepOptional.get().getMessage();
    }

    
    @Override
    public MessageFlowStepDto recordMessageAccepted(MessageConsumer messageConsumer, long messageFlowStepId, String contentType) {
        Message message = retrieveMessage(messageFlowStepId);
        
        MessageFlowStep messageFlowStep = createMessageFlowStep(message, (BaseMessagingComponent) messageConsumer, messageFlowStepId, null, MessageFlowStepActionType.ACCEPTED);
        messageFlowStep = messageFlowStepRepository.save(messageFlowStep);
        
        MessageFlowStepMapper mapper = new MessageFlowStepMapper();
        return mapper.doMapping(messageFlowStep);
    }

    
    @Override
    public MessageFlowStepDto recordInboundMessageProducedByOtherRoute(BaseInboundRouteConnector inboundRouteConnector, long messageFlowStepId,String contentType) {
        Message message = retrieveMessage(messageFlowStepId);
        
        MessageFlowStep messageFlowStep = createMessageFlowStep(message, inboundRouteConnector, messageFlowStepId, null, MessageFlowStepActionType.ACCEPTED);
        messageFlowStep = messageFlowStepRepository.save(messageFlowStep);

        MessageFlowStepMapper mapper = new MessageFlowStepMapper();
        return mapper.doMapping(messageFlowStep);
    }

    
    @Override
    public MessageFlowStepDto recordOutboundMessageToBeConsumedByOtherRoute(BaseOutboundRouteConnector outboundRouteConnector, long messageFlowStepId,String contentType, MessageFlowStepActionType action) {
        Message message = retrieveMessage(messageFlowStepId);
        
        MessageFlowStep messageFlowStep = createMessageFlowStep(message, outboundRouteConnector, messageFlowStepId, null, action);
        messageFlowStep = messageFlowStepRepository.save(messageFlowStep);

        MessageFlowStepMapper mapper = new MessageFlowStepMapper();
        return mapper.doMapping(messageFlowStep);
    }

    
    @Override
    public MessageFlowStepDto recordOutboundMessageHandlerComplete(String messageContent, BaseMessagingComponent messagingComponent,long messageFlowStepId, String contentType) {
        Message parentMessage = retrieveMessage(messageFlowStepId);
        
        // For a message dispatched by an outbound message handler we need to see if it is different from the inbound message.  If it is then create a new message object.
        Message outboundMessage = null;
        
        if (parentMessage.getContent().equals(messageContent)) {
            outboundMessage = parentMessage;
        } else {
            outboundMessage = new Message(messageContent, contentType);
        }
        
        MessageFlowStep messageFlowStep = createMessageFlowStep(outboundMessage, messagingComponent, messageFlowStepId, null, MessageFlowStepActionType.OUTBOUND_MESSAGE_HANDLING_COMPLETE);
        messageFlowStep = messageFlowStepRepository.save(messageFlowStep);

        MessageFlowStepMapper mapper = new MessageFlowStepMapper();
        return mapper.doMapping(messageFlowStep);
    }

    
    /**
     * Returns the specified message meta data value.
     * 
     * @param key
     * @param messageFlowStepId
     * @return
     */
    @Override
    public String retrieveMessageMetaData(String key, Long messageFlowStepId) {
        Optional<MessageFlowStep> messageFlowStepOptional = messageFlowStepRepository.findById(messageFlowStepId);
        
        if (messageFlowStepOptional.isEmpty()) {
            throw new RuntimeException("Message flow step id is not found");
        }
        
        MessageFlowStep messageFlowStep = messageFlowStepOptional.get();
        
        List<MessageMetaData>metaData = messageFlowStep.getMessage().getMetaData();
               
        for (MessageMetaData metaDataItem : metaData) {
            if (metaDataItem.getKey().equals(key)) {                
                return metaDataItem.getValue();
            }
        }
        
        return null;
    }

    @Override
    public void recordAck(String ackContent, BaseInboundAdapter inboundAdapter, Long fromMessageFlowStepId, String contentType) {
        Optional<MessageFlowStep> fromMessageFlowStep = messageFlowStepRepository.findById(fromMessageFlowStepId);

        if (fromMessageFlowStep.isEmpty()) {
            throw new RuntimeException("fromMessageFlowId not found.  This must be present to store the ACK.  Id: " + fromMessageFlowStepId);
        }
        
        // Create message object and associate it to a message flow.
        Message message = new Message(ackContent, contentType);
        createMessageFlowStep(message, inboundAdapter, fromMessageFlowStepId, null, MessageFlowStepActionType.ACK_SENT);
    }

    
    @Override
    public MessageFlowStepDto recordMessageReceivedFromExternalSource(String messageContent, BaseInboundAdapter inboundAdapter, String contentType) {
        Message message = new Message(messageContent, contentType);
        
        MessageFlowStep messageFlowStep = createMessageFlowStep(message, inboundAdapter, null, null, MessageFlowStepActionType.RECEIVED_BY_INBOUND_ADAPTER);
        messageFlowStep = messageFlowStepRepository.save(messageFlowStep);

        MessageFlowStepMapper mapper = new MessageFlowStepMapper();
        return mapper.doMapping(messageFlowStep);
    }
    
    
    @Override
    public MessageFlowStepDto recordMessageReceivedFromExternalSource(String messageContent, BaseInboundAdapter inboundAdapter, String contentType, Map<String,String>metadata) {
        Message message = new Message(messageContent, contentType);
        
        MessageFlowStep messageFlowStep = createMessageFlowStep(message, inboundAdapter, null, metadata, MessageFlowStepActionType.RECEIVED_BY_INBOUND_ADAPTER);
        messageFlowStep = messageFlowStepRepository.save(messageFlowStep);

        MessageFlowStepMapper mapper = new MessageFlowStepMapper();
        return mapper.doMapping(messageFlowStep);       
    }
}
