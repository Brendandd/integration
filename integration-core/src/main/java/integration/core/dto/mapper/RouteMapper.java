package integration.core.dto.mapper;

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.configuration.IntegrationRoute;
import integration.core.dto.ComponentDto;
import integration.core.dto.RouteDto;

/**
 * Maps a route domain object to a route dto
 * 
 * @author Brendan Douglas
 */
public class RouteMapper extends BaseMapper<RouteDto, IntegrationRoute> {

    @Override
    public RouteDto doMapping(IntegrationRoute source) {
        RouteDto destination = new RouteDto();

        destination.setId(source.getId());
        destination.setName(source.getName());
        destination.setOwner(source.getOwner());
        
        for (IntegrationComponent component : source.getComponents()) {
            ComponentDto componentDto = new ComponentDto();
            componentDto.setId(component.getId());
            componentDto.setName(component.getName());
            componentDto.setType(component.getType());
            componentDto.setCategory(component.getCategory());

            destination.addComponent(componentDto);
        }

        return destination;
    }
}
