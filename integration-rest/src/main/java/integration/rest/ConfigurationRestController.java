package integration.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import integration.core.dto.ComponentDto;
import integration.core.dto.RouteDto;
import integration.core.exception.ConfigurationException;
import integration.core.service.ConfigurationService;
import integration.core.service.impl.StatusChangeResponse;

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
        return configurationService.getAllRoutes();
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
        return configurationService.getRoute(id);
    }

    
    /**
     * Get all components.
     * 
     * @return
     * @throws ConfigurationException
     * @throws RetryableException 
     */
    @GetMapping(value = "/components")
    @ResponseStatus(HttpStatus.OK)
    public List<ComponentDto> getComponents() throws ConfigurationException, ConfigurationException {
        return configurationService.getAllComponents();
    }

    
    /**
     * Gets a single component by id.
     * 
     * @param routeName
     * @return
     * @throws ConfigurationException
     * @throws RetryableException 
     */
    @GetMapping(value = "/component/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ComponentDto getComponent(@PathVariable("id") long id) throws ConfigurationException, ConfigurationException {
        return configurationService.getComponent(id);
    }

    
    /**
     * Stops the components inbound handler so it cannot accept messages.
     * 
     * @param id
     * @return
     * @throws ConfigurationException
     * @throws RetryableException 
     */
    @PostMapping(value = "/component/{id}/stop/inbound")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StatusChangeResponse> stopComponentInbound(@PathVariable("id") long id) throws ConfigurationException {
        StatusChangeResponse response = configurationService.stopComponentInbound(id);
        
        return ResponseEntity.ok(response);
    }

    
    /**
     * Starts the components inbound handler.
     * 
     * @param id
     * @return
     * @throws ConfigurationException
     * @throws RetryableException 
     */
    @PostMapping(value = "/component/{id}/start/inbound")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StatusChangeResponse> startComponentInbound(@PathVariable("id") long id) throws ConfigurationException {
        StatusChangeResponse response = configurationService.startComponentInbound(id);
        
        return ResponseEntity.ok(response);
    }

    
    /**
     * Stops the components outbound handler so it cannot forward messages.
     * 
     * @param id
     * @return
     * @throws ConfigurationException
     * @throws RetryableException 
     */
    @PostMapping(value = "/component/{id}/stop/outbound")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StatusChangeResponse> stopComponentOutbound(@PathVariable("id") long id) throws ConfigurationException {
        StatusChangeResponse response = configurationService.stopComponentOutbound(id);
        
        return ResponseEntity.ok(response);
    }

    
    /**
     * Starts the components outbound handler.
     * 
     * @param id
     * @return
     * @throws ConfigurationException
     * @throws RetryableException 
     */
    @PostMapping(value = "/component/{id}/start/outbound")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StatusChangeResponse> startComponentOutbound(@PathVariable("id") long id) throws ConfigurationException {
        StatusChangeResponse response = configurationService.startComponentOutbound(id);
        
        return ResponseEntity.ok(response);
    }
}
