package integration.core.service.impl;

import java.util.HashMap;
import java.util.Map;

import integration.core.domain.messaging.MessageFlowStepActionType;
import integration.core.messaging.component.BaseMessagingComponent;

public class MessageFlowRequest {
    private String messageContent;
    private BaseMessagingComponent component;
    private Long parentMessageFlowStepId;
    private String contentType;
    private Map<String,String>metaData = new HashMap<>();
    private MessageFlowStepActionType action;
    
    
    public String getMessageContent() {
        return messageContent;
    }
    
    
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
    
    
    public BaseMessagingComponent getComponent() {
        return component;
    }
    
    
    public void setComponent(BaseMessagingComponent component) {
        this.component = component;
    }
    
    
    public Long getParentMessageFlowStepId() {
        return parentMessageFlowStepId;
    }
    
    
    public void setParentMessageFlowStepId(Long parentMessageFlowStepId) {
        this.parentMessageFlowStepId = parentMessageFlowStepId;
    }
    
    
    public String getContentType() {
        return contentType;
    }
    
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    
    public Map<String, String> getMetaData() {
        return metaData;
    }
    
    
    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }
    
    
    public MessageFlowStepActionType getAction() {
        return action;
    }
    
    
    public void setAction(MessageFlowStepActionType action) {
        this.action = action;
    }
}
