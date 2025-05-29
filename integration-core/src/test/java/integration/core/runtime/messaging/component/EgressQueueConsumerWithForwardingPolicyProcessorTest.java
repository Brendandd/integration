package integration.core.runtime.messaging.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;

import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.exception.ComponentNotFoundException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;

/**
 * Tests the processor which is called after the message flow id is consumer from the egress queue.
 */
@ExtendWith(MockitoExtension.class)
public class EgressQueueConsumerWithForwardingPolicyProcessorTest extends BaseMessageFlowProcessorTest {
    
    @InjectMocks
    private EgressQueueConsumerWithForwardingPolicyProcessor processor;

    
    
    @BeforeEach
    void setup() {
        processor.setComponent(component);
    }

    /**
     * Tests the successful execution of the processor.  The message is pending forwarding.
     * 
     * @throws Exception
     */
    @Test
    void testProcessor_MessagePendingForwarding() throws Exception {
        long forwardedMessageFlowId = 71l;
               
        mockCamelMessage();

        // Call the mock message flow service to get a mock dto.
        when(messageFlowService.retrieveMessageFlow(parentMessageFlowId)).thenReturn(parentMessageFlowDto);

        // Mock the mock message forwarding policy.
        mockSuccessMessageForwardingPolicy();

        // The forwarding result was success so record a message flow.
        when(parentMessageFlowDto.getId()).thenReturn(parentMessageFlowId);
        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenReturn(forwardedMessageFlowDto);
        when(forwardedMessageFlowDto.getId()).thenReturn(forwardedMessageFlowId);

        // Call the processor.
        processor.process(exchange);

        // Verify expected behavior
        verify(outboxService).recordEvent(forwardedMessageFlowId, componentId, OutboxEventType.PENDING_FORWARDING);
        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
    }

    
    /**
     * Tests the successful execution of the processor.  The message is not forwarding (filtered).
     * 
     * @throws Exception
     */
    @Test
    void testProcessor_MessageNotForwarded() throws Exception {
        long componentId = 555l;
        long parentMessageFlowId = 70l;
               
        // Mock the camel exchange message to return a mock message.
        mockCamelMessage();

        // Call the mock message flow service to get a mock dto.
        when(messageFlowService.retrieveMessageFlow(parentMessageFlowId)).thenReturn(parentMessageFlowDto);

        // Mock the mock message forwarding policy.
        MessageFlowPolicyResult failResult = mockNotSuccessMessageForwardingPolicy();

        // The forwarding result was not a success so record a not forwarded step.
        when(parentMessageFlowDto.getId()).thenReturn(parentMessageFlowId);

        // Call the processor.
        processor.process(exchange);

        // Verify expected behavior
        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), any());
        verify(messageFlowService, never()).recordMessageFlowWithSameContent(anyLong(), anyLong(), any());
        verify(messageFlowService).recordMessageNotForwarded(componentId,parentMessageFlowId,failResult,MessageFlowActionType.NOT_FORWARDED);
    }

    
    /**
     * Tests the throwing of a component not found exception.  This type of exception will have the retry flag set to false.
     * 
     * @throws Exception
     */
    @Test
    void testProcessor_ComponentNotFoundExceptionThrown() throws Exception {
        long componentId = 555l;
        long parentMessageFlowId = 70l;
               
        // Mock the camel exchange message to return a mock message.
        mockCamelMessage();

        // Call the mock message flow service to get a mock dto.
        when(messageFlowService.retrieveMessageFlow(parentMessageFlowId)).thenReturn(parentMessageFlowDto);

        // Mock the mock message forwarding policy.
        mockSuccessMessageForwardingPolicy();

        // The forwarding result was not a success so record a not forwarded step.
        when(parentMessageFlowDto.getId()).thenReturn(parentMessageFlowId);

        // Call the processor and throw an exception
        ComponentNotFoundException ex = new ComponentNotFoundException(555l);
        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenThrow(ex);
        
        ComponentNotFoundException thrown = assertThrows(
                ComponentNotFoundException.class,
                () -> processor.process(exchange)
            );
        
        assertFalse(thrown.isRetryable());

        // Make sure an event is not record when an exception is thrown.
        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), any());
        
        // Makes sure ta message not forwarded message flow step is not recorded when an exception is thrown.
        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
    }
    
    
    
    /**
     * Tests the throwing of a query timeout exception.  This type of exception will have the retry flag set to true.
     * 
     * @throws Exception
     */
    @Test
    void testProcessor_QueryTimeoutExceptionThrown() throws Exception {
        long componentId = 555l;
        long parentMessageFlowId = 70l;
               
        // Mock the camel exchange message to return a mock message.
        mockCamelMessage();

        // Call the mock message flow service to get a mock dto.
        when(messageFlowService.retrieveMessageFlow(parentMessageFlowId)).thenReturn(parentMessageFlowDto);

        // Mock the mock message forwarding policy.
        mockSuccessMessageForwardingPolicy();

        // The forwarding result was not a success so record a not forwarded step.
        when(parentMessageFlowDto.getId()).thenReturn(parentMessageFlowId);

        // Call the processor and throw an exception
        Throwable queryTimeOutException = new QueryTimeoutException("Query timed out");
        DataAccessException dataAccessException = new DataAccessException("DB error", queryTimeOutException) {};
        MessageFlowProcessingException ex = new MessageFlowProcessingException("Message flow error", parentMessageFlowId, dataAccessException);
        
        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenThrow(ex);
        
        MessageFlowProcessingException thrown = assertThrows(
                MessageFlowProcessingException.class,
                () -> processor.process(exchange)
            );
        
        assertTrue(thrown.isRetryable());

        // Make sure an event is not record when an exception is thrown.
        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), any());
        
        // Makes sure ta message not forwarded message flow step is not recorded when an exception is thrown.
        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
    }
    
    
    
    /**
     * Tests the throwing of a query timeout exception.  This type of exception will have the retry flag set to true.
     * 
     * @throws Exception
     */
    @Test
    void testProcessor_DataIntegrityViolationExceptionThrown() throws Exception {
        long componentId = 555l;
        long parentMessageFlowId = 70l;
               
        // Mock the camel exchange message to return a mock message.
        mockCamelMessage();

        // Call the mock message flow service to get a mock dto.
        when(messageFlowService.retrieveMessageFlow(parentMessageFlowId)).thenReturn(parentMessageFlowDto);

        // Mock the mock message forwarding policy.
        mockSuccessMessageForwardingPolicy();

        // The forwarding result was not a success so record a not forwarded step.
        when(parentMessageFlowDto.getId()).thenReturn(parentMessageFlowId);

        // Call the processor and throw an exception
        Throwable queryTimeOutException = new DataIntegrityViolationException("Duplicate key");
        DataAccessException dataAccessException = new DataAccessException("DB error", queryTimeOutException) {};
        MessageFlowProcessingException ex = new MessageFlowProcessingException("Message flow error", parentMessageFlowId, dataAccessException);
        
        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenThrow(ex);
        
        MessageFlowProcessingException thrown = assertThrows(
                MessageFlowProcessingException.class,
                () -> processor.process(exchange)
            );
        
        assertFalse(thrown.isRetryable());

        // Make sure an event is not record when an exception is thrown.
        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), any());
        
        // Makes sure ta message not forwarded message flow step is not recorded when an exception is thrown.
        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
    }
}
