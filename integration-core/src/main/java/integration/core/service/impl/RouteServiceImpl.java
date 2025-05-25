package integration.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import integration.core.domain.IdentifierType;
import integration.core.domain.configuration.IntegrationRoute;
import integration.core.dto.RouteDto;
import integration.core.dto.mapper.RouteMapper;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.RouteNotFoundException;
import integration.core.repository.RouteRepository;
import integration.core.runtime.messaging.exception.retryable.RouteAccessException;
import integration.core.service.RouteService;

@Component
@Transactional(propagation = Propagation.REQUIRED)
public class RouteServiceImpl implements RouteService {
    
    @Autowired
    private RouteRepository routeRepository;

    
    @Override
    public List<RouteDto> getAllRoutes() throws RouteAccessException  {
        try {
            List<IntegrationRoute> routes = routeRepository.getAllRoutes();
    
            List<RouteDto> routeDtos = new ArrayList<>();
    
            for (IntegrationRoute route : routes) {
                RouteMapper routeMapper = new RouteMapper();
    
                RouteDto routeDto = routeMapper.doMapping(route);
                routeDtos.add(routeDto);
            }
            
            return routeDtos;
        } catch(DataAccessException e) {
            throw new RouteAccessException("Database error while getting all routes", e);
        }
    }

    
    @Override
    public RouteDto getRoute(long routeId) throws RouteNotFoundException, RouteAccessException {
        try {
            Optional<IntegrationRoute> routeOptional = routeRepository.findById(routeId);
    
            if (routeOptional.isEmpty()) {
                List<ExceptionIdentifier>identifiers = new ArrayList<>();
                identifiers.add(new ExceptionIdentifier(IdentifierType.ROUTE_ID, routeId));
                throw new RouteNotFoundException(routeId);
            }
            
            RouteMapper mapper = new RouteMapper();
            
            return mapper.doMapping(routeOptional.get());
        } catch(DataAccessException e) {
            throw new RouteAccessException("Database error while getting a route by id", routeId, e);
        }
    }
}
