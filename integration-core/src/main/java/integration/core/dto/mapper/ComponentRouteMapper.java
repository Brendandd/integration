package integration.core.dto.mapper;

import integration.core.domain.configuration.ComponentRoute;
import integration.core.dto.ComponentRouteDto;

/**
 * Maps a component route domain object to a component route dto.
 * 
 * @author Brendan Douglas
 */
public class ComponentRouteMapper extends BaseMapper<ComponentRouteDto, ComponentRoute> {

    @Override
    public ComponentRouteDto doMapping(ComponentRoute source) {
        ComponentRouteDto destination = new ComponentRouteDto();
        destination.setId(source.getId());

        ComponentMapper componentMapper = new ComponentMapper();
        RouteMapper routeMapper = new RouteMapper();

        destination.setComponent(componentMapper.doMapping(source.getComponent()));
        destination.setRoute(routeMapper.doMapping(source.getRoute()));

        return destination;
    }
}
