package integration.core.runtime.messaging.service.impl;

import java.util.HashMap;
import java.util.Map;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;

public class MessageFlowRequest {
    private String messageContent;
    private long componentId;
    private Long parentMessageFlowId;
    private ContentTypeEnum contentType;
    private Map<String,String>properties = new HashMap<>();
    private MessageFlowActionType action;
    private Map<String, Object>headers;
    
    
    public String getMessageContent() {
        return messageContent;
    }
    
    
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
    
    
    public long getComponentId() {
        return componentId;
    }
    
    
    public void setComponentId(long componentId) {
        this.componentId = componentId;
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


    public Map<String, Object> getHeaders() {
        return headers;
    }

    
    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
}
