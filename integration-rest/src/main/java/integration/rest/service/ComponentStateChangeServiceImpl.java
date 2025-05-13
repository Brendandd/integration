package integration.rest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.exception.ConfigurationException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.repository.ComponentRepository;
import integration.rest.service.impl.ComponentStateChangeService;
import integration.rest.service.impl.StatusChangeResponse;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class ComponentStateChangeServiceImpl implements ComponentStateChangeService {

    @Autowired
    private ComponentRepository componentRepository;
       
   
    @Override
    public StatusChangeResponse stopComponentInbound(long id) throws ConfigurationException {
        try {
            Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
            
            if (componentOptional.isEmpty()) {
                List<ExceptionIdentifier>identifiers = new ArrayList<>();
                identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, id));
                throw new ConfigurationException("Component not found", identifiers,false);
            }
            
            IntegrationComponent component = componentOptional.get();
            
            if (component.getInboundState() == IntegrationComponentStateEnum.RUNNING) {
                component.setInboundState(IntegrationComponentStateEnum.STOPPED);
                componentRepository.save(component);
                
                return new StatusChangeResponse(true, "Inbound State Change", id, IntegrationComponentStateEnum.RUNNING, IntegrationComponentStateEnum.STOPPED);
            } 
    
            return new StatusChangeResponse(true, "Inbound already stopped", id, IntegrationComponentStateEnum.STOPPED, IntegrationComponentStateEnum.STOPPED);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, id));
            throw new ConfigurationException("Database error while stopping a components inbound", identifiers, e);
        }
    }

    
    @Override
    public StatusChangeResponse startComponentInbound(long id) throws ConfigurationException {
        try {
            Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
            
            if (componentOptional.isEmpty()) {
                List<ExceptionIdentifier>identifiers = new ArrayList<>();
                identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, id));
                throw new ConfigurationException("Component not found", identifiers, false);
            }
            
            IntegrationComponent component = componentOptional.get();
            
            if (component.getInboundState() == IntegrationComponentStateEnum.STOPPED) {
                component.setInboundState(IntegrationComponentStateEnum.RUNNING);
                componentRepository.save(component);
                
                return new StatusChangeResponse(true, "Inbound State Change", id, IntegrationComponentStateEnum.STOPPED, IntegrationComponentStateEnum.RUNNING);
            } 
            
            return new StatusChangeResponse(true, "Inbound already started", id, IntegrationComponentStateEnum.RUNNING, IntegrationComponentStateEnum.RUNNING);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, id));
            throw new ConfigurationException("Database error while starting a components inbound", identifiers,e);
        }
    }

    
    @Override
    public StatusChangeResponse stopComponentOutbound(long id) throws ConfigurationException {
        try {
            Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
            
            if (componentOptional.isEmpty()) {
                List<ExceptionIdentifier>identifiers = new ArrayList<>();
                identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, id));
                throw new ConfigurationException("Component not found", identifiers, false);
            }
            
            IntegrationComponent component = componentOptional.get();
            
            if (component.getOutboundState() == IntegrationComponentStateEnum.RUNNING) {
                component.setOutboundState(IntegrationComponentStateEnum.STOPPED);
                componentRepository.save(component);
                
                return new StatusChangeResponse(true, "Outbound State Change", id, IntegrationComponentStateEnum.RUNNING, IntegrationComponentStateEnum.STOPPED);
            }  
            
            return new StatusChangeResponse(true, "Outbound already stopped", id, IntegrationComponentStateEnum.STOPPED, IntegrationComponentStateEnum.STOPPED);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, id));
            throw new ConfigurationException("Database error while stopping a components outbound", identifiers, e);
        }
    }

    
    @Override
    public StatusChangeResponse startComponentOutbound(long id) throws ConfigurationException {
        try {
            Optional<IntegrationComponent>componentOptional = componentRepository.findById(id);
            
            if (componentOptional.isEmpty()) {
                List<ExceptionIdentifier>identifiers = new ArrayList<>();
                identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, id));
                throw new ConfigurationException("Component not found", identifiers, false);
            }
            
            IntegrationComponent component = componentOptional.get();
            
            if (component.getOutboundState() == IntegrationComponentStateEnum.STOPPED) {
                component.setOutboundState(IntegrationComponentStateEnum.RUNNING);
                componentRepository.save(component);
                
                return new StatusChangeResponse(true, "Outbound State Change", id, IntegrationComponentStateEnum.STOPPED, IntegrationComponentStateEnum.RUNNING);
            } 
            
            return new StatusChangeResponse(true, "Outbound already started", id, IntegrationComponentStateEnum.RUNNING, IntegrationComponentStateEnum.RUNNING);
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, id));
            throw new ConfigurationException("Database error while stopping a components outbound", identifiers, e);
        }
    }
}
