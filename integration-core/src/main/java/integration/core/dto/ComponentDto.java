package integration.core.dto;

import java.util.ArrayList;
import java.util.List;

import integration.core.domain.configuration.IntegrationComponentCategoryEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;

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
    private IntegrationComponentTypeEnum type;
    private IntegrationComponentCategoryEnum category;
    private String owner;
    private IntegrationComponentStateEnum inboundState;
    private IntegrationComponentStateEnum outboundState;
    
    private List<ComponentPropertyDto>properties = new ArrayList<>();
    
    private RouteDto route;
  
    public String getName() {
        return name;
    }
    
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    public IntegrationComponentTypeEnum getType() {
        return type;
    }
    
    
    public void setType(IntegrationComponentTypeEnum type) {
        this.type = type;
    }
    
    
    public IntegrationComponentCategoryEnum getCategory() {
        return category;
    }
    
    
    public void setCategory(IntegrationComponentCategoryEnum category) {
        this.category = category;
    }


    public String getOwner() {
        return owner;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }


    public IntegrationComponentStateEnum getInboundState() {
        return inboundState;
    }


    public void setInboundState(IntegrationComponentStateEnum inboundState) {
        this.inboundState = inboundState;
    }


    public IntegrationComponentStateEnum getOutboundState() {
        return outboundState;
    }


    public void setOutboundState(IntegrationComponentStateEnum outboundState) {
        this.outboundState = outboundState;
    }


    public RouteDto getRoute() {
        return route;
    }


    public void setRoute(RouteDto route) {
        this.route = route;
    }


    public List<ComponentPropertyDto> getProperties() {
        return properties;
    }


    public void setProperties(List<ComponentPropertyDto> properties) {
        this.properties = properties;
    } 
    
    public void addProperty(String key, String value) {
        ComponentPropertyDto property = new ComponentPropertyDto(key, value);
        properties.add(property);
    }
    
    
    public void addProperty(ComponentPropertyDto property) {
        properties.add(property);
    }
}