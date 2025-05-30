package integration.core.runtime.messaging.component;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import integration.core.domain.IdentifierType;

/**
 * Tests the processor which is called after the message flow id is consumer from the egress queue.
 */
@ExtendWith(MockitoExtension.class)
public class EgressQueueProducerProcessorTest extends BaseMessageFlowProcessorTest {
    
    @InjectMocks
    private EgressQueueProducerProcessor processor;

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
    void testProcessor_Success() throws Exception {              
        mockEventIdCamelMessage();

        when(camelMessage.getHeader(IdentifierType.MESSAGE_FLOW_ID.name())).thenReturn(parentMessageFlowId);
        
        when(messageConsumer.getIdentifier()).thenReturn(componentId);

        // Call the processor.
        processor.process(exchange);
        
        verify(outboxService).deleteEvent(eventId);
        verify(producerTemplate).sendBody("jms:queue:egressQueue-" + componentId, parentMessageFlowId);
    }
}
