package integration.rest.service.impl;

import integration.core.domain.configuration.IntegrationComponentStateEnum;

public class StatusChangeResponse {
    private boolean success;
    private String message;
    private long componentId;
    private IntegrationComponentStateEnum oldState;
    private IntegrationComponentStateEnum newState;
    
    public StatusChangeResponse(boolean success, String message, long componentId, IntegrationComponentStateEnum oldState, IntegrationComponentStateEnum newState) {
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



    public IntegrationComponentStateEnum getOldState() {
        return oldState;
    }



    public void setOldState(IntegrationComponentStateEnum oldState) {
        this.oldState = oldState;
    }



    public IntegrationComponentStateEnum getNewState() {
        return newState;
    }



    public void setNewState(IntegrationComponentStateEnum newState) {
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
