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

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.messaging.Message;
import integration.core.domain.messaging.MessageFlowEvent;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.domain.messaging.MessageFlowGroup;
import integration.core.domain.messaging.MessageFlowStep;
import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.domain.messaging.MessageFlowStepFiltered;
import integration.core.domain.messaging.MessageMetaData;
import integration.core.dto.MessageFlowEventDto;
import integration.core.dto.MessageFlowStepDto;
import integration.core.dto.mapper.MessageFlowEventMapper;
import integration.core.dto.mapper.MessageFlowStepMapper;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.BaseMessagingComponent;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;
import integration.core.repository.ComponentRepository;
import integration.core.repository.MessageFlowEventRepository;
import integration.core.repository.MessageFlowRepository;
import integration.core.repository.MessageFlowStepRepository;
import integration.core.repository.MessageRepository;
import integration.core.service.MessagingFlowService;

@Service
@Transactional(propagation = Propagation.REQUIRED)
public class MessagingFlowServiceImpl implements MessagingFlowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingFlowServiceImpl.class);

    @Autowired
    private MessageFlowStepRepository messageFlowStepRepository;
    
    @Autowired
    private MessageFlowRepository messageFlowRepository;
    
    @Autowired
    private MessageRepository messageRepository;
        
    @Autowired
    private MessageFlowEventRepository eventRepository;
    
    @Autowired
    private ComponentRepository componentRepository;

    
    @Override
    public void recordMessageFlowEvent(long messageFlowId, String componentPath, String owner, MessageFlowEventType eventType) {
        MessageFlowStep messageFlow = findMessageFlowById(messageFlowId);

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
    public MessageFlowStepDto recordMessageNotAccepted(MessageConsumer component, long parentMessageFlowStepId,MessageFlowPolicyResult policyResult, MessageFlowStepActionType action) {        
        return filterMessage((BaseMessagingComponent)component, parentMessageFlowStepId, policyResult, action);
    }

    
    @Override
    public MessageFlowStepDto recordMessageNotForwarded(MessageProducer component, long parentMessageFlowStepId,MessageFlowPolicyResult policyResult, MessageFlowStepActionType action) {        
        return filterMessage((BaseMessagingComponent)component, parentMessageFlowStepId, policyResult, action);
    }

    
    private MessageFlowStepDto filterMessage(BaseMessagingComponent component, long parentMessageFlowStepId,MessageFlowPolicyResult policyResult, MessageFlowStepActionType action) {  
        MessageFlowRequest request = new MessageFlowRequest();
        request.setComponent(component);
        request.setParentMessageFlowStepId(parentMessageFlowStepId);
        request.setAction(action);
        
        MessageFlowStepDto messageFlowStepDto = recordMessageFlowStep(request);
        
        Optional<MessageFlowStep> messageFlowOptional = messageFlowStepRepository.findById(messageFlowStepDto.getId());       
        MessageFlowStep messageFlowStep = messageFlowOptional.get();
               
        // Create the filter object.
        MessageFlowStepFiltered filter = new MessageFlowStepFiltered();
        filter.setName(policyResult.getFilterName());
        filter.setReason(policyResult.getFilterReason());
        filter.setMessageFlowStep(messageFlowStep);
        
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
    public MessageFlowStepDto recordMessageFlowStep(String messageContent, BaseMessagingComponent component, String contentType, Map<String,String>metaData, MessageFlowStepActionType action) {       
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponent(component);
        request.setContentType(contentType);
        request.setMetaData(metaData);
        request.setAction(action);
        
        return recordMessageFlowStep(request);
    }


    @Override
    public MessageFlowStepDto recordMessageFlowStep(String messageContent, BaseMessagingComponent component, long parentMessageFlowStepId, String contentType, Map<String,String>metaData, MessageFlowStepActionType action) {
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setMessageContent(messageContent);
        request.setComponent(component);
        request.setParentMessageFlowStepId(parentMessageFlowStepId);
        request.setContentType(contentType);
        request.setMetaData(metaData);
        request.setAction(action);
        
        return recordMessageFlowStep(request);
    }


    @Override
    public MessageFlowStepDto recordMessageFlowStep(BaseMessagingComponent component, long parentMessageFlowStepId, Map<String,String>metaData, MessageFlowStepActionType action) {
        MessageFlowRequest request = new MessageFlowRequest();
        
        request.setComponent(component);
        request.setParentMessageFlowStepId(parentMessageFlowStepId);
        request.setMetaData(metaData);
        request.setAction(action);
        
        return recordMessageFlowStep(request);
    }

    
    private MessageFlowStepDto recordMessageFlowStep(MessageFlowRequest request) {
        String messageContent = request.getMessageContent();
        BaseMessagingComponent component = request.getComponent();
        Long parentMessageFlowStepId = request.getParentMessageFlowStepId();
        String contentType = request.getContentType();
        Map<String,String>metaData = request.getMetaData();
        MessageFlowStepActionType action = request.getAction();
        
        if (component == null) {
            throw new RuntimeException("Component must not be null");
        }
        
        if (messageContent == null && parentMessageFlowStepId == null) {
            throw new RuntimeException("Either message or parentMessageFlowId must not be null");
        }

        
        // Get the parent message flow step if an id was supplied.
        MessageFlowStep parentMessageFlowStep = null;
        
        if (parentMessageFlowStepId != null) {
            Optional<MessageFlowStep> parentFlowStepOptional = messageFlowRepository.findById(parentMessageFlowStepId);
            parentMessageFlowStep = parentFlowStepOptional.get();
        }
        
        
        // If the message was not supplied then create it from the parent message flow id if that was supplied.
        Message message = null;
        
        if (parentMessageFlowStep != null) {
            if (messageContent == null) {
                message = parentMessageFlowStep.getMessage();
                message.setMetaData(parentMessageFlowStep.getMessage().getMetaData());  // Copy the metadata to the new message.                
            } else {
                // If message content was supplied then compare against the parent message.  If different then create a new message.
                if (!messageContent.equals(parentMessageFlowStep.getMessage().getContent())) {
                    message = new Message(messageContent, contentType);
                } 
            }
        } else {
            // There is no parent so store the original message.
            message = new Message(messageContent, contentType);
        }

        Optional<IntegrationComponent> integrationComponent = componentRepository.findById(component.getIdentifier());
        
        MessageFlowStep messageFlowStep = new MessageFlowStep();
        messageFlowStep.setComponent(integrationComponent.get());
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

        MessageFlowStep savedStep = messageFlowStepRepository.save(messageFlowStep);
        
        MessageFlowStepMapper mapper = new MessageFlowStepMapper();
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
