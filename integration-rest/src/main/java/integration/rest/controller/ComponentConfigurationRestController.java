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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import integration.core.dto.ComponentDto;
import integration.core.exception.ComponentNotFoundException;
import integration.core.runtime.messaging.exception.retryable.ComponentAccessException;
import integration.core.service.ComponentService;
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
    private ComponentService componentConfigurationService;
    
    @ExceptionHandler(ComponentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ComponentNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Component Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    
    /**
     * Get all components.
     * 
     * @return
     * @throws ComponentAccessException 
     */
    @GetMapping(value = "/components")
    public List<ComponentDto> getComponents() throws ComponentAccessException {
        return componentConfigurationService.getAllComponents();
    }

    
    /**
     * Gets a single component by id.
     * 
     * @param routeName
     * @return
     * @throws ComponentAccessException 
     * @throws ComponentNotFoundException 
     */
    @GetMapping(value = "/component/{id}")
    public ComponentDto getComponent(@PathVariable("id") long id) throws ComponentNotFoundException, ComponentAccessException {
        return componentConfigurationService.getComponent(id);
    }

    
    /**
     * Stops the components inbound handler so it cannot accept messages.
     * 
     * @param id
     * @return
     * @throws ComponentAccessException 
     * @throws ComponentNotFoundException 
     */
    @PostMapping(value = "/component/{id}/stop/inbound")
    public ResponseEntity<StatusChangeResponse> stopComponentInbound(@PathVariable("id") long id) throws ComponentNotFoundException, ComponentAccessException {
        StatusChangeResponse response = componentStateChangeService.stopComponentInbound(id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Starts the components inbound handler.
     * 
     * @param id
     * @return
     * @throws ComponentAccessException 
     * @throws ComponentNotFoundException 
     */
    @PostMapping(value = "/component/{id}/start/inbound")
    public ResponseEntity<StatusChangeResponse> startComponentInbound(@PathVariable("id") long id) throws ComponentNotFoundException, ComponentAccessException  {
        StatusChangeResponse response = componentStateChangeService.startComponentInbound(id);
        
        return ResponseEntity.ok(response);
    }

    
    /**
     * Stops the components outbound handler so it cannot forward messages.
     * 
     * @param id
     * @return
     * @throws ComponentAccessException 
     * @throws ComponentNotFoundException 
     */
    @PostMapping(value = "/component/{id}/stop/outbound")
    public ResponseEntity<StatusChangeResponse> stopComponentOutbound(@PathVariable("id") long id) throws ComponentNotFoundException, ComponentAccessException {
        StatusChangeResponse response = componentStateChangeService.stopComponentOutbound(id);
        
        return ResponseEntity.ok(response);
    }

    
    /**
     * Starts the components outbound handler.
     * 
     * @param id
     * @return
     * @throws ComponentAccessException 
     * @throws ComponentNotFoundException 
     */
    @PostMapping(value = "/component/{id}/start/outbound")
    public ResponseEntity<StatusChangeResponse> startComponentOutbound(@PathVariable("id") long id) throws ComponentNotFoundException, ComponentAccessException {
        StatusChangeResponse response = componentStateChangeService.startComponentOutbound(id);
        
        return ResponseEntity.ok(response);
    } 
}
