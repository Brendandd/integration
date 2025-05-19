package integration.rest.controller;

public class ComponentPropertyChangeRequest {
    private long componentId;
    private long propertyId;
    private String newValue;
    
    
    public long getComponentId() {
        return componentId;
    }
    
    
    public void setComponentId(long componentId) {
        this.componentId = componentId;
    }
    
    
    public long getPropertyId() {
        return propertyId;
    }
    
    
    public void setPropertyId(long propertyId) {
        this.propertyId = propertyId;
    }
    
    
    public String getNewValue() {
        return newValue;
    }
    
    
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}
