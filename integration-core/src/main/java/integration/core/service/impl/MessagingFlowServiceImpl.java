package integration.core.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.messaging.Message;
import integration.core.domain.messaging.MessageFlow;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEvent;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowFiltered;
import integration.core.domain.messaging.MessageFlowGroup;
import integration.core.domain.messaging.MessageFlowProperty;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.MessageFlowEventDto;
import integration.core.dto.mapper.MessageFlowEventMapper;
import integration.core.dto.mapper.MessageFlowMapper;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.BaseMessagingComponent;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;
import integration.core.repository.ComponentRepository;
import integration.core.repository.MessageFlowEventRepository;
import integration.core.repository.MessageFlowRepository;
import integration.core.repository.MessageRepository;
import integration.core.service.MessageFlowPropertyService;
import integration.core.service.MessagingFlowService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MessagingFlowServiceImpl implements MessagingFlowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingFlowServiceImpl.class);

    @Autowired
    private MessageFlowRepository messageFlowRepository;
       
    @Autowired
    private MessageRepository messageRepository;
        
    @Autowired
    private MessageFlowEventRepository eventRepository;
    
    @Autowired
    private ComponentRepository componentRepository;
    
    @Autowired
    private MessageFlowPropertyService messageFlowPropertyService;

    
    @Override
    public void recordMessageFlowEvent(long messageFlowId, String componentPath, String owner, MessageFlowEventType eventType) {
        MessageFlow messageFlow = findMessageFlowById(messageFlowId);

        MessageFlowEvent event = new MessageFlowEvent();
        event.setMessageFlow(messageFlow);
        event.setType(eventType);
        event.setComponentPath(componentPath);
        event.setOwner(owner);
        
        eventRepository.save(event);
    }

    
    /**
     * Retrieves a message flowDto by id.
     */
    @Override
    public MessageFlowDto retrieveMessageFlow(long messageFlowId) {       
        Optional<MessageFlow> messageFlow = messageFlowRepository.findById(messageFlowId);

        // The message flow must exist.
        if (messageFlow.isEmpty()) {
            throw new ConfigurationException("Message flow not found. Id: " + messageFlowId);
        }

        MessageFlowMapper mapper = new MessageFlowMapper();

        return mapper.doMapping(messageFlow.get());
    }
    
    
    private MessageFlow findMessageFlowById(long messageFlowId) {
        Optional<MessageFlow> messageFlow = messageFlowRepository.findById(messageFlowId);

        // The message flow must exist.
        if (messageFlow.isEmpty()) {
            throw new ConfigurationException("Message flow not found. Id: " + messageFlowId);
        }

        return messageFlow.get();
    }

    
    @Override
    public List<MessageFlowEventDto> getEventsForComponent(String owner, int numberToRead, String componentPath) {
        MessageFlowEventMapper mapper = new MessageFlowEventMapper();

        List<MessageFlowEventDto> eventDtos = new ArrayList<>();

        List<MessageFlowEvent> events = eventRepository.getEvents(owner, componentPath, numberToRead);

        for (MessageFlowEvent event : events) {
            eventDtos.add(mapper.doMapping(event));
        }
        
        return eventDtos;
    }

    
    @Override
    public List<MessageFlowEventDto> getEvents(String owner, String componentPath) {
        MessageFlowEventMapper mapper = new MessageFlowEventMapper();

        List<MessageFlowEventDto> eventDtos = new ArrayList<>();

        List<MessageFlowEvent> events = eventRepository.getEvents(owner, componentPath);

        for (MessageFlowEvent event : events) {
            eventDtos.add(mapper.doMapping(event));
        }
        
        return eventDtos;
    }

    
    @Override
    public void deleteEvent(long eventId) {
        eventRepository.deleteById(eventId);
    }

    
    @Override
    public MessageFlowDto recordMessageNotAccepted(MessageConsumer component, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) {        
        return filterMessage((BaseMessagingComponent)component, parentMessageFlowId, policyResult, action);
    }

    
    @Override
    public MessageFlowDto recordMessageNotForwarded(MessageProducer component, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) {        
        return filterMessage((BaseMessagingComponent)component, parentMessageFlowId, policyResult, action);
    }

    
    private MessageFlowDto filterMessage(BaseMessagingComponent component, long parentMessageFlowId,MessageFlowPolicyResult policyResult, MessageFlowActionType action) {  
        MessageFlowRequest request = new MessageFlowRequest();
        request.setComponent(component);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setAction(action);
        
        MessageFlowDto messageFlowDto = recordMessageFlow(request);
        
        Optional<MessageFlow> messageFlowOptional = messageFlowRepository.findById(messageFlowDto.getId());       
        MessageFlow messageFlow = messageFlowOptional.get();
               
        // Create the filter object.
        MessageFlowFiltered filter = new MessageFlowFiltered();
        filter.setName(policyResult.getFilterName());
        filter.setReason(policyResult.getFilterReason());
        filter.setMessageFlow(messageFlow);
        
        messageFlow = messageFlowRepository.save(messageFlow);
        
        MessageFlowMapper mapper = new MessageFlowMapper();
        return mapper.doMapping(messageFlow);
    }

    
    /**
     * Returns the specified message flow property.
     * 
     * @param key
     * @param messageFlowId
     * @return
     */
    @Override
    public String getMessageFlowProperty(String key, Long messageFlowId) {
        Optional<MessageFlow> messageFlowOptional = messageFlowRepository.findById(messageFlowId);
        
        if (messageFlowOptional.isEmpty()) {
            throw new RuntimeException("Message flow id is not found");
        }
        
        MessageFlow messageFlow = messageFlowOptional.get();
        
        List<MessageFlowProperty>properties = messageFlow.getProperties();
               
        for (MessageFlowProperty property : properties) {
            if (property.getKey().equals(key)) {                
                return property.getValue();
            }
        }
        
        return null;
    }


    @Override
    public MessageFlowDto recordMessageFlow(String messageContent, BaseMessagingComponent component, ContentTypeEnum contentType, MessageFlowActionType action) {       
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponent(component);
        request.setContentType(contentType);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }


    @Override
    public MessageFlowDto recordMessageFlow(String messageContent, BaseMessagingComponent component, long parentMessageFlowId, ContentTypeEnum contentType, MessageFlowActionType action) {
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponent(component);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setContentType(contentType);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }


    @Override
    public MessageFlowDto recordMessageFlow(BaseMessagingComponent component, long parentMessageFlowId, MessageFlowActionType action) {
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setComponent(component);
        request.setParentMessageFlowId(parentMessageFlowId);
        request.setAction(action);
        
        return recordMessageFlow(request);
    }

    
    private MessageFlowDto recordMessageFlow(MessageFlowRequest request) {
        String messageContent = request.getMessageContent();
        BaseMessagingComponent component = request.getComponent();
        Long parentMessageFlowId = request.getParentMessageFlowId();
        ContentTypeEnum contentType = request.getContentType();
        Map<String,String>properties = request.getProperties();
        MessageFlowActionType action = request.getAction();
        
        if (component == null) {
            throw new RuntimeException("Component must not be null");
        }
        
        if (messageContent == null && parentMessageFlowId == null) {
            throw new RuntimeException("Either message or parentMessageFlowId must not be null");
        }

        
        // Get the parent message flow if an id was supplied.
        MessageFlow parentMessageFlow = null;
        
        if (parentMessageFlowId != null) {
            Optional<MessageFlow> parentFlowStepOptional = messageFlowRepository.findById(parentMessageFlowId);
            parentMessageFlow = parentFlowStepOptional.get();
        }

        // If the message was not supplied then create it from the parent message flow id if that was supplied.
        Message message = null;
        
        if (parentMessageFlow != null) {
            if (messageContent == null) {
                message = parentMessageFlow.getMessage();                 
            } else {
                // If message content was supplied then compare against the parent message.  If different then create a new message.
                if (!messageContent.equals(parentMessageFlow.getMessage().getContent())) {
                    message = new Message(messageContent, contentType);
                } 
            }
        } else {
            // There is no parent so store the original message.
            message = new Message(messageContent, contentType);
        }
        
        Optional<IntegrationComponent> integrationComponent = componentRepository.findById(component.getIdentifier());
        
        MessageFlow messageFlow = new MessageFlow();
        messageFlow.setComponent(integrationComponent.get());
        messageFlow.setMessage(message);
        messageFlow.setAction(action);

        // Associate the new message flow with its parent.
        if (parentMessageFlow != null) {
            messageFlow.setParentMessageFlow(parentMessageFlow);
            
            // Copy all properties
            for (MessageFlowProperty property : parentMessageFlow.getProperties()) {
                messageFlow.addOrUpdateProperty(property.getKey(), property.getValue());
            }
        }

        MessageFlowGroup group = null;
        
        // If the parent is null this is the original message so a new group needs creating.
        if (parentMessageFlow == null) {
            group = new MessageFlowGroup();
        } else {
            group = parentMessageFlow.getGroup();
        }
        
        group.addMessageFlow(messageFlow);
        
        MessageFlow savedStep = messageFlowRepository.save(messageFlow);
        
        MessageFlowMapper mapper = new MessageFlowMapper();
        return mapper.doMapping(savedStep);
    }

    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setEventFailed(long eventId) {
        Optional<MessageFlowEvent> eventOptional =  eventRepository.findById(eventId);
        
        MessageFlowEvent event = eventOptional.get();
        
        int retryCount = event.getRetryCount();
        event.setRetryCount(++retryCount);
        
        Calendar calendar = Calendar.getInstance();
        
        if (event.getRetryAfter() == null) {
            calendar.setTime(new Date());
        }
        

        int delaySeconds = 30 * retryCount;
        calendar.add(Calendar.SECOND, delaySeconds);
        
        event.setRetryAfter(calendar.getTime());
        
        eventRepository.save(event);   
    }
}
