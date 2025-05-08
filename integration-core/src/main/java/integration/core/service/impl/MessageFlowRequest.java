package integration.core.service.impl;

import java.util.HashMap;
import java.util.Map;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.messaging.component.BaseMessagingComponent;

public class MessageFlowRequest {
    private String messageContent;
    private BaseMessagingComponent component;
    private Long parentMessageFlowId;
    private ContentTypeEnum contentType;
    private Map<String,String>properties = new HashMap<>();
    private MessageFlowActionType action;
    
    
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
    
    
    public Long getParentMessageFlowId() {
        return parentMessageFlowId;
    }
    
    
    public void setParentMessageFlowId(Long parentMessageFlowId) {
        this.parentMessageFlowId = parentMessageFlowId;
    }
    
    
    public ContentTypeEnum getContentType() {
        return contentType;
    }
    
    
    public void setContentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
    }
    
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    
    public MessageFlowActionType getAction() {
        return action;
    }
    
    
    public void setAction(MessageFlowActionType action) {
        this.action = action;
    }
}
