package integration.rest.controller;

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
import integration.core.exception.ConfigurationException;
import integration.core.service.ComponentConfigurationService;
import integration.rest.service.impl.ComponentStateChangeService;
import integration.rest.service.impl.StatusChangeResponse;


/**
 * A rest controller for component configuration.
 * 
 * TODO add more APIs.
 * 
 * @author Brendan Douglas
 *
 */
@RestController
@RequestMapping("/configuration")
public class ComponentConfigurationRestController {

    @Autowired
    protected ComponentStateChangeService componentStateChangeService;
    
    @Autowired
    private ComponentConfigurationService componentConfigurationService;

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
        return componentConfigurationService.getAllComponents();
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
        return componentConfigurationService.getComponent(id);
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
        StatusChangeResponse response = componentStateChangeService.stopComponentInbound(id);
        
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
        StatusChangeResponse response = componentStateChangeService.startComponentInbound(id);
        
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
        StatusChangeResponse response = componentStateChangeService.stopComponentOutbound(id);
        
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
        StatusChangeResponse response = componentStateChangeService.startComponentOutbound(id);
        
        return ResponseEntity.ok(response);
    }
}
