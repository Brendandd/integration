package integration.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;

import integration.core.domain.configuration.IntegrationRoute;
import integration.core.dto.RouteDto;
import integration.core.exception.RouteNotFoundException;
import integration.core.repository.RouteRepository;
import integration.core.runtime.messaging.exception.retryable.RouteAccessException;
import integration.core.service.impl.RouteServiceImpl;
import jakarta.persistence.EntityManager;

/**
 * Some tests for the route service.
 */
@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private RouteServiceImpl routeService;

    @Test
    void getAllRoutes_whenDataExists_returnsRouteList() throws RouteAccessException {
        IntegrationRoute mockRoute = new IntegrationRoute();
        mockRoute.setId(1L);
        mockRoute.setName("A test route");

        when(routeRepository.getAllRoutes()).thenReturn(Collections.singletonList(mockRoute));

        List<RouteDto> result = routeService.getAllRoutes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A test route", result.get(0).getName());
    }

    @Test
    void getAllRoutes_whenQueryTimeout_throwsRetryableException() {
        Throwable cause = new QueryTimeoutException("Query timed out");
        DataAccessException exceptionCaught = new DataAccessException("DB error", cause) {};

        when(routeRepository.getAllRoutes()).thenThrow(exceptionCaught);

        RouteAccessException ex = assertThrows(RouteAccessException.class, () -> {
            routeService.getAllRoutes();
        });

        assertTrue(ex.isRetryable(), "This exception should retry");
    }

    @Test
    void getAllRoutes_whenDataIntegrityViolation_throwsNonRetryableException() {
        Throwable cause = new DataIntegrityViolationException("Duplicate key");
        DataAccessException exceptionCaught = new DataAccessException("DB error", cause) {};

        when(routeRepository.getAllRoutes()).thenThrow(exceptionCaught);

        RouteAccessException ex = assertThrows(RouteAccessException.class, () -> {
            routeService.getAllRoutes();
        });

        assertFalse(ex.isRetryable(), "This exception should not retry");
    }

    @Test
    void getRoute_whenRouteExists_returnsRouteDto() throws Exception {
        long routeId = 1L;
        IntegrationRoute mockRoute = new IntegrationRoute();
        mockRoute.setId(1L);
        mockRoute.setName("A test route");

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(mockRoute));

        RouteDto result = routeService.getRoute(routeId);

        assertNotNull(result);
        assertEquals("A test route", result.getName());
    }

    @Test
    void getRoute_whenRouteDoesNotExists_throwsRouteNotFoundException() {
        long routeId = 123L;
        when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

        RouteNotFoundException ex = assertThrows(RouteNotFoundException.class, () -> {
            routeService.getRoute(routeId);
        });

        assertFalse(ex.isRetryable(), "A route not found will never be retried");
    }

    @Test
    void getRoute_whenQueryTimeout_throwsRetryableException() {
        long routeId = 1L;
        Throwable cause = new QueryTimeoutException("Query timed out");
        DataAccessException exceptionCaught = new DataAccessException("DB error", cause) {};

        when(routeRepository.findById(routeId)).thenThrow(exceptionCaught);

        RouteAccessException ex = assertThrows(RouteAccessException.class, () -> {
            routeService.getRoute(routeId);
        });

        assertTrue(ex.isRetryable(), "This exception should retry");
    }

    @Test
    void getRoute_whenDataIntegrityViolation_throwsNonRetryableException() {
        long routeId = 1L;
        Throwable cause = new DataIntegrityViolationException("Duplicate key");
        DataAccessException exceptionCaught = new DataAccessException("DB error", cause) {};

        when(routeRepository.findById(routeId)).thenThrow(exceptionCaught);

        RouteAccessException ex = assertThrows(RouteAccessException.class, () -> {
            routeService.getRoute(routeId);
        });

        assertFalse(ex.isRetryable(), "This exception should not retry");
    }
}
