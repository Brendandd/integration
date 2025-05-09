package integration.core.dto;

import integration.core.domain.configuration.ComponentCategoryEnum;
import integration.core.domain.configuration.ComponentStateEnum;
import integration.core.domain.configuration.ComponentTypeEnum;

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
    private ComponentTypeEnum type;
    private ComponentCategoryEnum category;
    private String owner;
    private ComponentStateEnum inboundState;
    private ComponentStateEnum outboundState;
    
    private RouteDto route;
  
    public String getName() {
        return name;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    public ComponentTypeEnum getType() {
        return type;
    }
    
    
    public void setType(ComponentTypeEnum type) {
        this.type = type;
    }
    
    
    public ComponentCategoryEnum getCategory() {
        return category;
    }
    
    
    public void setCategory(ComponentCategoryEnum category) {
        this.category = category;
    }


    public String getOwner() {
        return owner;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }


    public ComponentStateEnum getInboundState() {
        return inboundState;
    }


    public void setInboundState(ComponentStateEnum inboundState) {
        this.inboundState = inboundState;
    }


    public ComponentStateEnum getOutboundState() {
        return outboundState;
    }


    public void setOutboundState(ComponentStateEnum outboundState) {
        this.outboundState = outboundState;
    }


    public RouteDto getRoute() {
        return route;
    }


    public void setRoute(RouteDto route) {
        this.route = route;
    } 
}