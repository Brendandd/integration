package integration.core.dto.mapper;

import integration.core.domain.configuration.Route;
import integration.core.dto.RouteDto;

/**
 * Maps a route domain object to a route dto
 * 
 * @author Brendan Douglas
 */
public class RouteMapper extends BaseMapper<RouteDto, Route> {

    @Override
    public RouteDto doMapping(Route source) {
        RouteDto destination = new RouteDto();

        destination.setDescription(source.getDescription());
        destination.setId(source.getId());
        destination.setName(source.getName());

        return destination;
    }
}
