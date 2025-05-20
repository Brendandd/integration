package integration.core.service;

import java.util.List;

import integration.core.dto.RouteDto;
import integration.core.exception.RouteAccessException;
import integration.core.exception.RouteNotFoundException;

/**
 * A service for routes.
 * 
 * @author Brendan Douglas
 *
 */
public interface RouteService {
    List<RouteDto> getAllRoutes() throws RouteAccessException;
    
    RouteDto getRoute(long routeId) throws RouteNotFoundException, RouteAccessException;
}


