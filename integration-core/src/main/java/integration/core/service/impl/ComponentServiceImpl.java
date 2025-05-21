package integration.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.dto.ComponentDto;
import integration.core.dto.mapper.ComponentMapper;
import integration.core.exception.ComponentNotFoundException;
import integration.core.repository.ComponentRepository;
import integration.core.runtime.messaging.exception.retryable.ComponentAccessException;
import integration.core.service.ComponentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class ComponentServiceImpl implements ComponentService {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ComponentRepository componentRepository;
           
    @Override
    public List<ComponentDto> getAllComponents() throws ComponentAccessException {
        try {
            List<IntegrationComponent>components = componentRepository.getAllComponents();
            
            List<ComponentDto>componentDtos = new ArrayList<>();
            
            for (IntegrationComponent component : components) {
                ComponentMapper componentMapper = new ComponentMapper();
                
                ComponentDto componentDto = componentMapper.doMapping(component);
                componentDtos.add(componentDto);
            }
            return componentDtos;
        } catch(DataAccessException e) {
            throw new ComponentAccessException("Database error while getting all components", e);
        }
    }

    
    @Override
    public ComponentDto getComponent(long componentId) throws ComponentNotFoundException, ComponentAccessException {
        try {
            Optional<IntegrationComponent> componentOptional = componentRepository.findById(componentId);
    
            if (componentOptional.isEmpty()) {
                throw new ComponentNotFoundException(componentId);
            }
            
            ComponentMapper mapper = new ComponentMapper();
            
            return mapper.doMapping(componentOptional.get());
        } catch(DataAccessException e) {
            throw new ComponentAccessException("Database error while getting a component", componentId, e);
        }
    }

    
//    @Override
//    public void updateProperty(long componentId, long propertyId) {
//        // TODO Auto-generated method stub
//        
//    }
}
