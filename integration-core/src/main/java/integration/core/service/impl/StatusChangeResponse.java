package integration.core.service.impl;

import integration.core.domain.configuration.ComponentStateEnum;

public class StatusChangeResponse {
    private boolean success;
    private String message;
    private long componentId;
    private ComponentStateEnum oldState;
    private ComponentStateEnum newState;
    
    public StatusChangeResponse(boolean success, String message, long componentId, ComponentStateEnum oldState, ComponentStateEnum newState) {
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



    public ComponentStateEnum getOldState() {
        return oldState;
    }



    public void setOldState(ComponentStateEnum oldState) {
        this.oldState = oldState;
    }



    public ComponentStateEnum getNewState() {
        return newState;
    }



    public void setNewState(ComponentStateEnum newState) {
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
