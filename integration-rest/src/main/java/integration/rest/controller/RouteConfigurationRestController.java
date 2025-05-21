package integration.rest.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import integration.core.dto.RouteDto;
import integration.core.exception.RouteNotFoundException;
import integration.core.runtime.messaging.exception.retryable.RouteAccessException;
import integration.core.service.RouteService;


/**
 * A rest controller for route configuration.
 * 
 * TODO add more APIs.
 * 
 * @author Brendan Douglas
 *
 */
@RestController
@RequestMapping("/configuration")
public class RouteConfigurationRestController {
   
    @Autowired
    private RouteService routeService;

    
    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RouteNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Route Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    
    /**
     * Get all routes.
     * 
     * @return
     * @throws RouteAccessException 
     * @throws RetryableException 
     */
    @GetMapping(value = "/routes")
    public List<RouteDto> getAllRoutes() throws RouteAccessException {
        return routeService.getAllRoutes();
    }

    
    /**
     * Gets a single route by id.
     * 
     * @param routeName
     * @return
     * @throws RetryableException 
     * @throws RouteNotFoundException 
     * @throws RouteAccessException 
     */
    @GetMapping(value = "/route/{id}")
    public RouteDto getRoute(@PathVariable("id") long id) throws RouteNotFoundException, RouteAccessException  {
        return routeService.getRoute(id);
    }
}
