package integration.core.runtime.messaging.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.exception.ComponentNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;

/**
 * Tests the processor which is called after the message flow id is consumer from the egress queue.
 */
@ExtendWith(MockitoExtension.class)
public class EgressQueueConsumerWithoutForwardingPolicyProcessorTest extends BaseMessageFlowProcessorTest {
    
    @InjectMocks
    private EgressQueueConsumerWithoutForwardingPolicyProcessor processor;

    @BeforeEach
    void setup() {
        processor.setComponent(messageConsumer);
    }

    /**
     * Tests the successful execution of the processor.  The message is pending forwarding.
     * 
     * @throws Exception
     */
    @Test
    void testProcessor_MessagePendingForwarding() throws Exception {
        long forwardedMessageFlowId = 71L;
               
        mockMessageFlowIdCamelMessage();

        // Call the mock message flow service to get a mock dto.
        mockRetrieveMessageFlowWithoutContent();
        
        when(messageConsumer.getIdentifier()).thenReturn(componentId);
        when(messageConsumer.getRoute()).thenReturn(route);
        when(messageConsumer.getRoute().getIdentifier()).thenReturn(routeId);
        when(messageConsumer.getOwner()).thenReturn("Mock Owner");

        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenReturn(forwardedMessageFlowDto);
        when(forwardedMessageFlowDto.getId()).thenReturn(forwardedMessageFlowId);

        // Call the processor.
        processor.process(exchange);

        // Verify expected behavior
        verify(outboxService).recordEvent(forwardedMessageFlowId, componentId, routeId, owner, OutboxEventType.PENDING_FORWARDING);
        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
    }

    
    /**
     * Tests the throwing of a component not found exception.  This type of exception will have the retry flag set to false.
     * 
     * @throws Exception
     */
    @Test
    void testProcessor_ComponentNotFoundExceptionThrown() throws Exception {
               
        // Mock the camel exchange message to return a mock message.
        mockMessageFlowIdCamelMessage();

        // Call the mock message flow service to get a mock dto.
        mockRetrieveMessageFlowWithoutContent();
        
        when(messageConsumer.getIdentifier()).thenReturn(componentId);

        // Call the processor and throw an exception
        ComponentNotFoundException ex = new ComponentNotFoundException(555L);
        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenThrow(ex);
        
        ComponentNotFoundException thrown = assertThrows(
                ComponentNotFoundException.class,
                () -> processor.process(exchange)
            );
        
        assertFalse(thrown.isRetryable());

        // Make sure an event is not record when an exception is thrown.
        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), anyLong(), anyString(), any());
        
        // Makes sure a message not forwarded message flow step is not recorded when an exception is thrown.
        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
    }
    
    
    
    /**
     * Tests the throwing of a query timeout exception.  This type of exception will have the retry flag set to true.
     * 
     * @throws Exception
     */
    @Test
    void testProcessor_QueryTimeoutExceptionThrown() throws Exception {
               
        // Mock the camel exchange message to return a mock message.
        mockMessageFlowIdCamelMessage();

        // Call the mock message flow service to get a mock dto.
        mockRetrieveMessageFlowWithoutContent();
        
        when(messageConsumer.getIdentifier()).thenReturn(componentId);

        MessageFlowProcessingException ex = mockRetryableException();
        
        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenThrow(ex);
        
        MessageFlowProcessingException thrown = assertThrows(
                MessageFlowProcessingException.class,
                () -> processor.process(exchange)
            );
        
        assertTrue(thrown.isRetryable());

        // Make sure an event is not record when an exception is thrown.
        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), anyLong(), anyString(), any());
        
        // Makes sure a message not forwarded message flow step is not recorded when an exception is thrown.
        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
    }
    
    
    
    /**
     * Tests the throwing of a query timeout exception.  This type of exception will have the retry flag set to true.
     * 
     * @throws Exception
     */
    @Test
    void testProcessor_DataIntegrityViolationExceptionThrown() throws Exception {
               
        // Mock the camel exchange message to return a mock message.
        mockMessageFlowIdCamelMessage();

        // Call the mock message flow service to get a mock dto.
        mockRetrieveMessageFlowWithoutContent();
        
        when(messageConsumer.getIdentifier()).thenReturn(componentId);

        MessageFlowProcessingException ex = mockNotRetryableException();
        
        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenThrow(ex);
        
        MessageFlowProcessingException thrown = assertThrows(
                MessageFlowProcessingException.class,
                () -> processor.process(exchange)
            );
        
        assertFalse(thrown.isRetryable());

        // Make sure an event is not record when an exception is thrown.
        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), anyLong(), anyString(), any());
        
        // Makes sure a message not forwarded message flow step is not recorded when an exception is thrown.
        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
    }
}
