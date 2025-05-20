package integration.rest.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.exception.ComponentAccessException;
import integration.core.exception.ComponentNotFoundException;
import integration.core.repository.ComponentRepository;
import integration.rest.service.impl.ComponentStateChangeService;
import integration.rest.service.impl.StatusChangeResponse;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class ComponentStateChangeServiceImpl implements ComponentStateChangeService {

    @Autowired
    private ComponentRepository componentRepository;
       
   
    @Override
    public StatusChangeResponse stopComponentInbound(long id) throws ComponentNotFoundException, ComponentAccessException {
        try {
            Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
            
            if (componentOptional.isEmpty()) {
                throw new ComponentNotFoundException(id);
            }
            
            IntegrationComponent component = componentOptional.get();
            
            if (component.getInboundState() == IntegrationComponentStateEnum.RUNNING) {
                component.setInboundState(IntegrationComponentStateEnum.STOPPED);
                componentRepository.save(component);
                
                return new StatusChangeResponse(true, "Inbound State Change", id, IntegrationComponentStateEnum.RUNNING, IntegrationComponentStateEnum.STOPPED);
            } 
    
            return new StatusChangeResponse(true, "Inbound already stopped", id, IntegrationComponentStateEnum.STOPPED, IntegrationComponentStateEnum.STOPPED);
        } catch(DataAccessException e) {
            throw new ComponentAccessException("Database error while stopping a components inbound", id, e);
        }
    }

    
    @Override
    public StatusChangeResponse startComponentInbound(long id) throws ComponentNotFoundException, ComponentAccessException {
        try {
            Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
            
            if (componentOptional.isEmpty()) {
                throw new ComponentNotFoundException(id);
            }
            
            IntegrationComponent component = componentOptional.get();
            
            if (component.getInboundState() == IntegrationComponentStateEnum.STOPPED) {
                component.setInboundState(IntegrationComponentStateEnum.RUNNING);
                componentRepository.save(component);
                
                return new StatusChangeResponse(true, "Inbound State Change", id, IntegrationComponentStateEnum.STOPPED, IntegrationComponentStateEnum.RUNNING);
            } 
            
            return new StatusChangeResponse(true, "Inbound already started", id, IntegrationComponentStateEnum.RUNNING, IntegrationComponentStateEnum.RUNNING);
        } catch(DataAccessException e) {
            throw new ComponentAccessException("Database error while starting a components inbound", id,e);
        }
    }

    
    @Override
    public StatusChangeResponse stopComponentOutbound(long id) throws ComponentNotFoundException, ComponentAccessException {
        try {
            Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
            
            if (componentOptional.isEmpty()) {
                throw new ComponentNotFoundException(id);
            }
            
            IntegrationComponent component = componentOptional.get();
            
            if (component.getOutboundState() == IntegrationComponentStateEnum.RUNNING) {
                component.setOutboundState(IntegrationComponentStateEnum.STOPPED);
                componentRepository.save(component);
                
                return new StatusChangeResponse(true, "Outbound State Change", id, IntegrationComponentStateEnum.RUNNING, IntegrationComponentStateEnum.STOPPED);
            }  
            
            return new StatusChangeResponse(true, "Outbound already stopped", id, IntegrationComponentStateEnum.STOPPED, IntegrationComponentStateEnum.STOPPED);
        } catch(DataAccessException e) {
            throw new ComponentAccessException("Database error while stopping a components outbound", id, e);
        }
    }

    
    @Override
    public StatusChangeResponse startComponentOutbound(long id) throws ComponentNotFoundException, ComponentAccessException {
        try {
            Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
            
            if (componentOptional.isEmpty()) {
                throw new ComponentNotFoundException(id);
            }
            
            IntegrationComponent component = componentOptional.get();
            
            if (component.getOutboundState() == IntegrationComponentStateEnum.STOPPED) {
                component.setOutboundState(IntegrationComponentStateEnum.RUNNING);
                componentRepository.save(component);
                
                return new StatusChangeResponse(true, "Outbound State Change", id, IntegrationComponentStateEnum.STOPPED, IntegrationComponentStateEnum.RUNNING);
            } 
            
            return new StatusChangeResponse(true, "Outbound already started", id, IntegrationComponentStateEnum.RUNNING, IntegrationComponentStateEnum.RUNNING);
        } catch(DataAccessException e) {
            throw new ComponentAccessException("Database error while stopping a components outbound", id, e);
        }
    }
}
