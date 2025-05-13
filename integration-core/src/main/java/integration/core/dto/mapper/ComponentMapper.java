package integration.core.dto.mapper;

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.configuration.IntegrationComponentProperty;
import integration.core.dto.ComponentDto;
import integration.core.dto.ComponentPropertyDto;
import integration.core.dto.RouteDto;

/**
 * Maps a component domain object to a component dto.
 * 
 * @author Brendan Douglas
 */
public class ComponentMapper extends BaseMapper<ComponentDto, IntegrationComponent> {

    @Override
    public ComponentDto doMapping(IntegrationComponent source) {
        ComponentDto destination = new ComponentDto();
        destination.setId(source.getId());
        destination.setName(source.getName());
        destination.setType(source.getType());
        destination.setCategory(source.getCategory());
        destination.setOwner(source.getOwner());
        destination.setInboundState(source.getInboundState());
        destination.setOutboundState(source.getOutboundState());
        
        RouteDto routeDto = new RouteDto();
        routeDto.setId(source.getRoute().getId());
        routeDto.setName(source.getRoute().getName());
        
        destination.setRoute(routeDto);
        
        for (IntegrationComponentProperty property : source.getProperties()) {
            if (property.getEndDate() == null) {
                ComponentPropertyDto propertyDto = new ComponentPropertyDto(property.getKey(), property.getValue());
                destination.addProperty(propertyDto);
            }
        }
        
        return destination;
    }
}
