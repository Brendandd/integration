package integration.core.dto;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentState;
import integration.core.domain.configuration.ComponentType;

/**
 * 
 * A single component. A component is an adapter or a processing
 * step.
 * 
 * @author Brendan Douglas
 */
public class ComponentDto extends BaseDto {
    private static final long serialVersionUID = 1099848325612162806L;

    private String name;
    private ComponentType type;
    private ComponentCategory category;
    private String owner;
    private ComponentState inboundState;
    private ComponentState outboundState;
  
    public String getName() {
        return name;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    public ComponentType getType() {
        return type;
    }
    
    
    public void setType(ComponentType type) {
        this.type = type;
    }
    
    
    public ComponentCategory getCategory() {
        return category;
    }
    
    
    public void setCategory(ComponentCategory category) {
        this.category = category;
    }


    public String getOwner() {
        return owner;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }


    public ComponentState getInboundState() {
        return inboundState;
    }


    public void setInboundState(ComponentState inboundState) {
        this.inboundState = inboundState;
    }


    public ComponentState getOutboundState() {
        return outboundState;
    }


    public void setOutboundState(ComponentState outboundState) {
        this.outboundState = outboundState;
    } 
}