package integration.rest;

import java.util.List;

import javax.naming.ConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import integration.core.dto.RouteDto;
import integration.core.service.ConfigurationService;

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
public class ConfigurationRestController {

    @Autowired
    protected ConfigurationService configurationService;

    @GetMapping(value = "/routes")
    @ResponseStatus(HttpStatus.OK)
    public List<RouteDto> getAllRoutes() throws ConfigurationException {
        return configurationService.getAllRoutes();
    }

    @GetMapping(value = "/route/{routeName}")
    @ResponseStatus(HttpStatus.OK)
    public RouteDto getRouteByName(@PathVariable("routeName") String routeName) throws ConfigurationException {
        return configurationService.getRouteByName(routeName);
    }

    @GetMapping(value = "/route/{routeName}/status")
    @ResponseStatus(HttpStatus.OK)
    public RouteDto getRouteStatus(@PathVariable("routeName") String routeName) throws ConfigurationException {
        throw new UnsupportedOperationException("This method has not yet been implemented.");
    }
}
