//package integration.core.runtime.messaging.component;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import integration.core.domain.messaging.MessageFlowActionType;
//import integration.core.domain.messaging.OutboxEventType;
//import integration.core.exception.ComponentNotFoundException;
//import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
//import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
//
///**
// * Tests the processor which is called after the message flow id is consumer from the egress queue.
// */
//@ExtendWith(MockitoExtension.class)
//public class EgressQueueConsumerWithForwardingPolicyProcessorTest extends BaseMessageFlowProcessorTest {
//    
//    @InjectMocks
//    private EgressQueueConsumerWithForwardingPolicyProcessor processor;
//
//
//    @BeforeEach
//    void setup() {
//        processor.setComponent(messageProducer);
//    }
//
//    /**
//     * Tests the successful execution of the processor.  The message is pending forwarding.
//     * 
//     * @throws Exception
//     */
//    @Test
//    void testProcessor_MessagePendingForwarding() throws Exception {
//        long forwardedMessageFlowId = 71L;
//               
//        mockMessageFlowIdCamelMessage();
//
//        // Call the mock message flow service to get a mock dto.
//        when(messageFlowService.retrieveMessageFlow(parentMessageFlowId, true)).thenReturn(parentMessageFlowDto);
//        
//        when(messageProducer.getIdentifier()).thenReturn(componentId);
//        when(messageProducer.getRoute()).thenReturn(route);
//        when(messageProducer.getRoute().getIdentifier()).thenReturn(routeId);
//        when(messageProducer.getOwner()).thenReturn(owner);
//
//        // Mock the mock message forwarding policy.
//        mockSuccessMessageForwardingPolicy();
//
//        // The forwarding result was success so record a message flow.
//        when(parentMessageFlowDto.getId()).thenReturn(parentMessageFlowId);
//        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenReturn(forwardedMessageFlowDto);
//        when(forwardedMessageFlowDto.getId()).thenReturn(forwardedMessageFlowId);
//
//        // Call the processor.
//        processor.process(exchange);
//
//        // Verify expected behavior
//        verify(outboxService).recordEvent(forwardedMessageFlowId, componentId, routeId, owner,OutboxEventType.PENDING_FORWARDING);
//        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
//    }
//
//    
//    /**
//     * Tests the successful execution of the processor.  The message is not forwarding (filtered).
//     * 
//     * @throws Exception
//     */
//    @Test
//    void testProcessor_MessageNotForwarded() throws Exception {
//               
//        // Mock the camel exchange message to return a mock message.
//        mockMessageFlowIdCamelMessage();
//
//        // Call the mock message flow service to get a mock dto.
//        mockRetrieveMessageFlowWithContent();
//        
//        when(messageProducer.getIdentifier()).thenReturn(componentId);
//
//        // Mock the mock message forwarding policy.
//        MessageFlowPolicyResult failResult = mockNotSuccessMessageForwardingPolicy();
//
//        // Call the processor.
//        processor.process(exchange);
//
//        // Verify expected behavior
//        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), anyLong(), anyString(), any());
//        verify(messageFlowService, never()).recordMessageFlowWithSameContent(anyLong(), anyLong(), any());
//        verify(messageFlowService).recordMessageNotForwarded(componentId,parentMessageFlowId,failResult,MessageFlowActionType.NOT_FORWARDED);
//    }
//
//    
//    /**
//     * Tests the throwing of a component not found exception.  This type of exception will have the retry flag set to false.
//     * 
//     * @throws Exception
//     */
//    @Test
//    void testProcessor_ComponentNotFoundExceptionThrown() throws Exception {
//               
//        // Mock the camel exchange message to return a mock message.
//        mockMessageFlowIdCamelMessage();
//
//        // Call the mock message flow service to get a mock dto.
//        mockRetrieveMessageFlowWithContent();
//        
//        when(messageProducer.getIdentifier()).thenReturn(componentId);
//
//        // Mock the mock message forwarding policy.
//        mockSuccessMessageForwardingPolicy();
//
//
//        // Call the processor and throw an exception
//        ComponentNotFoundException ex = new ComponentNotFoundException(555L);
//        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenThrow(ex);
//        
//        ComponentNotFoundException thrown = assertThrows(
//                ComponentNotFoundException.class,
//                () -> processor.process(exchange)
//            );
//        
//        assertFalse(thrown.isRetryable());
//
//        // Make sure an event is not record when an exception is thrown.
//        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), anyLong(), anyString(), any());
//        
//        // Makes sure ta message not forwarded message flow step is not recorded when an exception is thrown.
//        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
//    }
//    
//    
//    
//    /**
//     * Tests the throwing of a query timeout exception.  This type of exception will have the retry flag set to true.
//     * 
//     * @throws Exception
//     */
//    @Test
//    void testProcessor_QueryTimeoutExceptionThrown() throws Exception {
//               
//        // Mock the camel exchange message to return a mock message.
//        mockMessageFlowIdCamelMessage();
//
//        // Call the mock message flow service to get a mock dto.
//        mockRetrieveMessageFlowWithContent();
//        
//        when(messageProducer.getIdentifier()).thenReturn(componentId);
//
//        // Mock the mock message forwarding policy.
//        mockSuccessMessageForwardingPolicy();
//
//        MessageFlowProcessingException ex = mockRetryableException();
//        
//        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenThrow(ex);
//        
//        MessageFlowProcessingException thrown = assertThrows(
//                MessageFlowProcessingException.class,
//                () -> processor.process(exchange)
//            );
//        
//        assertTrue(thrown.isRetryable());
//
//        // Make sure an event is not record when an exception is thrown.
//        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), anyLong(), anyString(), any());
//        
//        // Makes sure a message not forwarded message flow step is not recorded when an exception is thrown.
//        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
//    }
//    
//    
//    
//    /**
//     * Tests the throwing of a query timeout exception.  This type of exception will have the retry flag set to true.
//     * 
//     * @throws Exception
//     */
//    @Test
//    void testProcessor_DataIntegrityViolationExceptionThrown() throws Exception {
//              
//        // Mock the camel exchange message to return a mock message.
//        mockMessageFlowIdCamelMessage();
//
//        // Call the mock message flow service to get a mock dto.
//        mockRetrieveMessageFlowWithContent();
//        
//        when(messageProducer.getIdentifier()).thenReturn(componentId);
//
//        // Mock the mock message forwarding policy.
//        mockSuccessMessageForwardingPolicy();
//
//        MessageFlowProcessingException ex = mockNotRetryableException();
//        
//        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING)).thenThrow(ex);
//        
//        MessageFlowProcessingException thrown = assertThrows(
//                MessageFlowProcessingException.class,
//                () -> processor.process(exchange)
//            );
//        
//        assertFalse(thrown.isRetryable());
//
//        // Make sure an event is not record when an exception is thrown.
//        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), anyLong(), anyString(), any());
//        
//        // Makes sure a message not forwarded message flow step is not recorded when an exception is thrown.
//        verify(messageFlowService, never()).recordMessageNotForwarded(anyLong(), anyLong(), any(), any());
//    }
//}
