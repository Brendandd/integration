package integration.core.exception;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.dao.RecoverableDataAccessException;

import integration.core.runtime.messaging.exception.nonretryable.MessageFlowEventNotFoundException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.retryable.ComponentAccessException;
import integration.core.runtime.messaging.exception.retryable.RouteAccessException;

/**
 * Tests to see if the retry flag is set correctly on an exception based on it
 * cause and type.  Being able to retry when required and not retry when a retry would never work is very important.
 */
public class ExceptionTest {
    
    @Test
    public void testComponentNotFoundException() {
        // This type of exception can never be retried.
        ComponentNotFoundException exception = new ComponentNotFoundException(123l);
        assertFalse(exception.isRetryable());
    }

    
    @Test
    public void testRouteNotFoundException() {
     // This type of exception can never be retried.
        RouteNotFoundException exception = new RouteNotFoundException(123l);
        assertFalse(exception.isRetryable());
    }

    
    @Test
    public void testEventNotFoundException() {
     // This type of exception can never be retried.
        MessageFlowEventNotFoundException exception = new MessageFlowEventNotFoundException(123l);
        assertFalse(exception.isRetryable());
    }
    
    
    @Test
    public void testMesageFlowNotFoundException() {
     // This type of exception can never be retried.
        MessageFlowNotFoundException exception = new MessageFlowNotFoundException(123l);
        assertFalse(exception.isRetryable());
    }

    
    @Test
    public void testComponentAccessExceptionNoRetry() {
        // This will not retry as the cause is not a cause which will retry.
        ComponentAccessException exception = new ComponentAccessException("An issue accessing the component", 123l, new NullPointerException("A NPE"));
        assertFalse(exception.isRetryable());
    } 
    
    
    @Test
    public void testComponentAccessExceptionWillRetry() {
        // This will retry as the cause is an exception which might works after retry.
        ComponentAccessException exception = new ComponentAccessException("An issue accessing the component", 123l, new RecoverableDataAccessException("An error saving a component"));
        assertTrue(exception.isRetryable());
    } 
    
    
    @Test
    public void testRouteAccessExceptionNoRetry() {
        // This will not retry as the cause is not a cause which will retry.
        RouteAccessException exception = new RouteAccessException("An issue accessing the route", 123l, new NullPointerException("A NPE"));
        assertFalse(exception.isRetryable());
    } 
    
    
    @Test
    public void testRouteAccessExceptionWillRetry() {
        // This will retry as the cause is an exception which might works after retry.
        RouteAccessException exception = new RouteAccessException("An issue accessing the route", 123l, new RecoverableDataAccessException("An error saving a route"));
        assertTrue(exception.isRetryable());
    } 
    
    
    /**
     * An exception type which cannot be retried is the cause of an exception which can be retried.  Due to the non retry exception in the cause it will not retry.
     */
    @Test
    public void testNonRetryExceptionCauseInRetryableException() {
        // The cause is an exception which cannot be retried
        RouteNotFoundException cause = new RouteNotFoundException(123l);
        assertFalse(cause.isRetryable());
        
        // The exception being thrown can be retried, but due to the cause it will not retry.
        RouteAccessException exception = new RouteAccessException("An issue accessing the route", 123l, cause);
        assertFalse(exception.isRetryable()); 
    }
}
