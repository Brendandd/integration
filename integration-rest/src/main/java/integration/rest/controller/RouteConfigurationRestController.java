package integration.rest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import integration.core.dto.RouteDto;
import integration.core.exception.ConfigurationException;
import integration.core.service.RouteConfigurationService;


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
    private RouteConfigurationService routeConfigurationService;

    
    /**
     * Get all routes.
     * 
     * @return
     * @throws ConfigurationException
     * @throws integration.core.exception.ConfigurationException 
     * @throws RetryableException 
     */
    @GetMapping(value = "/routes")
    @ResponseStatus(HttpStatus.OK)
    public List<RouteDto> getAllRoutes() throws ConfigurationException {
        return routeConfigurationService.getAllRoutes();
    }

    
    /**
     * Gets a single route by id.
     * 
     * @param routeName
     * @return
     * @throws RetryableException 
     * @throws integration.core.exception.ConfigurationException 
     * @throws ConfigurationException
     */
    @GetMapping(value = "/route/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RouteDto getRoute(@PathVariable("id") long id) throws ConfigurationException  {
        return routeConfigurationService.getRoute(id);
    }
}
