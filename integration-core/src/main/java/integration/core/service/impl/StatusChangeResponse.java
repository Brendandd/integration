package integration.core.service.impl;

import integration.core.domain.configuration.ComponentState;

public class StatusChangeResponse {
    private boolean success;
    private String message;
    private long componentId;
    private ComponentState oldState;
    private ComponentState newState;
    
    public StatusChangeResponse(boolean success, String message, long componentId, ComponentState oldState, ComponentState newState) {
        this.success = success;
        this.message = message;
        this.componentId = componentId;
        this.oldState = oldState;
        this.newState = newState;
    }

    
    public boolean isSuccess() {
        return success;
    }



    public void setSuccess(boolean success) {
        this.success = success;
    }



    public String getMessage() {
        return message;
    }



    public void setMessage(String message) {
        this.message = message;
    }



    public long getComponentId() {
        return componentId;
    }



    public void setComponentId(long componentId) {
        this.componentId = componentId;
    }



    public ComponentState getOldState() {
        return oldState;
    }



    public void setOldState(ComponentState oldState) {
        this.oldState = oldState;
    }



    public ComponentState getNewState() {
        return newState;
    }



    public void setNewState(ComponentState newState) {
        this.newState = newState;
    }



    @Override
    public String toString() {
        return "ActionResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", componentId=" + componentId +
                ", oldState='" + oldState + '\'' +
                ", newState='" + newState + '\'' +
                '}';
    }   
}
