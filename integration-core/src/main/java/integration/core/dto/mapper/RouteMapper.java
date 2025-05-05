package integration.core.dto.mapper;

import integration.core.domain.configuration.IntegrationRoute;
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

        return destination;
    }
}
