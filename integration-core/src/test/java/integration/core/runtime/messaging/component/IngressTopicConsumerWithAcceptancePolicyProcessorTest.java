package integration.core.runtime.messaging.component;

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

import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;

/**
 */
@ExtendWith(MockitoExtension.class)
public class IngressTopicConsumerWithAcceptancePolicyProcessorTest extends BaseMessageFlowProcessorTest {
    
    @InjectMocks
    private IngressTopicConsumerWithAcceptancePolicyProcessor processor;
    
    
    @BeforeEach
    void setup() {
        processor.setComponent(messageConsumer);
    }

    
    @Test
    void testProcessor_MessageAccepted() throws Exception {
        long acceptedMessageFlowId = 71L;
        
        mockMessageFlowIdCamelMessage();
        
        // Call the mock message flow service to get a mock dto.
        mockRetrieveMessageFlow();
        
        when(messageConsumer.getIdentifier()).thenReturn(componentId);
        
        // Mock the mock message acceptance policy.
        mockSuccessMessageAcceptancePolicy();
        
        // The acceptance result was success so record a message flow.
        when(parentMessageFlowDto.getId()).thenReturn(parentMessageFlowId);
        when(messageFlowService.recordMessageFlowWithSameContent(componentId, parentMessageFlowId, MessageFlowActionType.ACCEPTED)).thenReturn(acceptedMessageFlowDto);
        when(acceptedMessageFlowDto.getId()).thenReturn(acceptedMessageFlowId);

        // Call the processor.
        processor.process(exchange);

        // Verify expected behavior
        verify(outboxService).recordEvent(acceptedMessageFlowId, componentId, OutboxEventType.INGRESS_COMPLETE);
        verify(messageFlowService, never()).recordMessageNotAccepted(anyLong(), anyLong(), any(), any());
    }

    
    @Test
    void testProcessor_MessageNotAccepted() throws Exception {       
        mockMessageFlowIdCamelMessage();
        
        // Call the mock message flow service to get a mock dto.
        mockRetrieveMessageFlow();
        
        when(messageConsumer.getIdentifier()).thenReturn(componentId);
        
        // Mock the mock message acceptance policy.
        MessageFlowPolicyResult notAcceptedResult = mockNotSuccessMessageAcceptancePolicy();
        
        // Call the processor.
        processor.process(exchange);

        // Verify expected behavior
        verify(outboxService, never()).recordEvent(anyLong(), anyLong(), any());
        verify(messageFlowService, never()).recordMessageFlowWithSameContent(anyLong(), anyLong(), any());
        verify(messageFlowService).recordMessageNotAccepted(componentId,parentMessageFlowId,notAcceptedResult,MessageFlowActionType.NOT_ACCEPTED);
    }

}
