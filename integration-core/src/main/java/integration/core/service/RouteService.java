package integration.core.service;

import java.util.List;

import integration.core.dto.RouteDto;
import integration.core.exception.RouteNotFoundException;
import integration.core.runtime.messaging.exception.retryable.RouteAccessException;

/**
 * A service for routes.
 * 
 * @author Brendan Douglas
 *
 */
public interface RouteService {
    
    /**
     * Returns all routes.
     * 
     * @return
     * @throws RouteAccessException
     */
    List<RouteDto> getAllRoutes() throws RouteAccessException;
    
    
    /**
     * Gets a route by id.
     * 
     * @param routeId
     * @return
     * @throws RouteNotFoundException
     * @throws RouteAccessException
     */
    RouteDto getRoute(long routeId) throws RouteNotFoundException, RouteAccessException;
}


