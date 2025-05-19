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
import integration.core.exception.ConfigurationException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.repository.ComponentRepository;
import integration.core.service.ComponentConfigurationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class ComponentConfigurationServiceImpl implements ComponentConfigurationService {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ComponentRepository componentRepository;
           
    @Override
    public List<ComponentDto> getAllComponents() throws ConfigurationException {
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
            throw new ConfigurationException("Database error while getting all components", new ArrayList<>(), e);
        }
    }

    
    @Override
    public ComponentDto getComponent(long componentId) throws ConfigurationException {
        try {
            Optional<IntegrationComponent> componentOptional = componentRepository.findById(componentId);
    
            if (componentOptional.isEmpty()) {
                throw new ComponentNotFoundException(componentId);
            }
            
            ComponentMapper mapper = new ComponentMapper();
            
            return mapper.doMapping(componentOptional.get());
        } catch(DataAccessException e) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, componentId));
            throw new ConfigurationException("Database error while getting a component", identifiers, e);
        }
    }

    
//    @Override
//    public void updateProperty(long componentId, long propertyId) {
//        // TODO Auto-generated method stub
//        
//    }
}
