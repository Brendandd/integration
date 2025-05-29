package integration.core.runtime.messaging.component;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.mockito.Mock;

import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.service.MessageFlowService;
import integration.core.runtime.messaging.service.OutboxService;

/**
 * Base class for all processors which require a message flow id.  This class just contains some helper methods.
 */
public class BaseMessageFlowProcessorTest {
    // Ids used for all tests.
    protected long componentId = 555l;
    protected long parentMessageFlowId = 70l;
    
    
    @Mock
    protected Exchange exchange;
    
    @Mock
    protected Message camelMessage; 
    
    @Mock
    protected OutboxService outboxService;

    @Mock
    protected MessageFlowService messageFlowService;

    @Mock
    protected MessageProducer component;

    @Mock
    protected MessageFlowDto parentMessageFlowDto;

    @Mock
    protected MessageFlowDto forwardedMessageFlowDto;

    @Mock
    protected MessageForwardingPolicy messageForwardingPolicy;
    
    protected void mockCamelMessage() {
        // Mock the camel exchange message to return a mock message.
        when(exchange.getMessage()).thenReturn(camelMessage);
        when(camelMessage.getBody(Long.class)).thenReturn(parentMessageFlowId);
        doNothing().when(camelMessage).setHeader(anyString(), any());
    }

    
    /**
     * A not successful forwarding policy.  This means the message is filtered.
     * 
     * @return
     * @throws ComponentConfigurationException
     * @throws FilterException
     */
    protected MessageFlowPolicyResult mockNotSuccessMessageForwardingPolicy() throws ComponentConfigurationException, FilterException {
        MessageFlowPolicyResult notSuccess = new MessageFlowPolicyResult(false);
        
        when(component.getMessageForwardingPolicy()).thenReturn(messageForwardingPolicy);
        when(component.getIdentifier()).thenReturn(componentId);
        when(messageForwardingPolicy.applyPolicy(parentMessageFlowDto)).thenReturn(notSuccess);
        
        return notSuccess;
    }

    

    /**
     * A successful forwarding policy.  This means the message is not forwarded.
     * 
     * @return
     * @throws ComponentConfigurationException
     * @throws FilterException
     */
    protected MessageFlowPolicyResult mockSuccessMessageForwardingPolicy() throws ComponentConfigurationException, FilterException {
        MessageFlowPolicyResult success = new MessageFlowPolicyResult(true);
        
        when(component.getMessageForwardingPolicy()).thenReturn(messageForwardingPolicy);
        when(component.getIdentifier()).thenReturn(componentId);
        when(messageForwardingPolicy.applyPolicy(parentMessageFlowDto)).thenReturn(success);
        
        return success;
    }
}
