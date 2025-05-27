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

import integration.core.domain.configuration.IntegrationComponent;
import integration.core.domain.configuration.IntegrationRoute;
import integration.core.dto.ComponentDto;
import integration.core.exception.ComponentNotFoundException;
import integration.core.repository.ComponentRepository;
import integration.core.runtime.messaging.exception.retryable.ComponentAccessException;
import integration.core.service.impl.ComponentServiceImpl;
import jakarta.persistence.EntityManager;

/**
 * Some tests for the component service.
 */
@ExtendWith(MockitoExtension.class)
class ComponentServiceImplTest {

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ComponentServiceImpl componentService;

    @Test
    void getAllComponents_whenDataExists_returnsComponentList() throws ComponentAccessException {
        IntegrationComponent mockComponent = new IntegrationComponent();
        mockComponent.setId(1L);
        mockComponent.setName("A test component");
        IntegrationRoute mockRoute = new IntegrationRoute();
        mockRoute.setId(1L);
        mockComponent.setRoute(mockRoute);

        when(componentRepository.getAllComponents()).thenReturn(Collections.singletonList(mockComponent));

        List<ComponentDto> result = componentService.getAllComponents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
    }

    @Test
    void getAllComponents_whenQueryTimeout_throwsRetryableException() {
        Throwable cause = new QueryTimeoutException("Query timed out");
        DataAccessException exceptionCaught = new DataAccessException("DB error", cause) {};

        when(componentRepository.getAllComponents()).thenThrow(exceptionCaught);

        ComponentAccessException ex = assertThrows(ComponentAccessException.class, () -> {
            componentService.getAllComponents();
        });

        assertTrue(ex.isRetryable(), "This exception should retry");
    }

    @Test
    void getAllComponents_whenDataIntegrityViolation_throwsNonRetryableException() {
        Throwable cause = new DataIntegrityViolationException("Duplicate key");
        DataAccessException exceptionCaught = new DataAccessException("DB error", cause) {};

        when(componentRepository.getAllComponents()).thenThrow(exceptionCaught);

        ComponentAccessException ex = assertThrows(ComponentAccessException.class, () -> {
            componentService.getAllComponents();
        });

        assertFalse(ex.isRetryable(), "This exception should not retry");
    }

    @Test
    void getComponent_whenComponentExists_returnsComponentDto() throws Exception {
        long componentId = 1L;
        IntegrationComponent mockComponent = new IntegrationComponent();
        mockComponent.setId(1L);
        mockComponent.setName("A test component");
        IntegrationRoute mockRoute = new IntegrationRoute();
        mockRoute.setId(1L);
        mockComponent.setRoute(mockRoute);

        when(componentRepository.findById(componentId)).thenReturn(Optional.of(mockComponent));

        ComponentDto result = componentService.getComponent(componentId);

        assertNotNull(result);
    }

    @Test
    void getComponent_whenComponentDoesNotExist_throwsComponentNotFoundException() {
        long componentId = 123L;
        when(componentRepository.findById(componentId)).thenReturn(Optional.empty());

        ComponentNotFoundException ex = assertThrows(ComponentNotFoundException.class, () -> {
            componentService.getComponent(componentId);
        });

        assertFalse(ex.isRetryable(), "A component not found will never be retried");
    }

    @Test
    void getComponent_whenQueryTimeout_throwsRetryableException() {
        long componentId = 1L;
        Throwable cause = new QueryTimeoutException("Query timed out");
        DataAccessException exceptionCaught = new DataAccessException("DB error", cause) {};

        when(componentRepository.findById(componentId)).thenThrow(exceptionCaught);

        ComponentAccessException ex = assertThrows(ComponentAccessException.class, () -> {
            componentService.getComponent(componentId);
        });

        assertTrue(ex.isRetryable(), "This exception should retry");
    }

    @Test
    void getComponent_whenDataIntegrityViolation_throwsNonRetryableException() {
        long componentId = 1L;
        Throwable cause = new DataIntegrityViolationException("Duplicate key");
        DataAccessException exceptionCaught = new DataAccessException("DB error", cause) {};

        when(componentRepository.findById(componentId)).thenThrow(exceptionCaught);

        ComponentAccessException ex = assertThrows(ComponentAccessException.class, () -> {
            componentService.getComponent(componentId);
        });

        assertFalse(ex.isRetryable(), "This exception should not retry");
    }
}
