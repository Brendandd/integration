package integration.core.runtime.messaging.component;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.mockito.Mock;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;

import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.nonretryable.OutboxEventNotFoundException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.exception.retryable.OutboxEventProcessingException;
import integration.core.runtime.messaging.service.MessageFlowService;
import integration.core.runtime.messaging.service.OutboxService;

/**
 * Base class for all processors which require a message flow id.  This class just contains some helper methods.
 */
public class BaseMessageFlowProcessorTest {
    // Ids used for all tests.
    protected long componentId = 555l;
    protected long parentMessageFlowId = 70l;
    protected long eventId = 25l;
    
    
    @Mock
    protected Exchange exchange;
    
    @Mock
    protected Message camelMessage; 
    
    @Mock
    protected OutboxService outboxService;

    @Mock
    protected MessageFlowService messageFlowService;

    @Mock
    protected MessageProducer messageProducer;
    
    @Mock
    protected MessageConsumer messageConsumer;

    @Mock
    protected MessageFlowDto parentMessageFlowDto;

    @Mock
    protected MessageFlowDto forwardedMessageFlowDto;
    
    @Mock
    protected MessageFlowDto acceptedMessageFlowDto;

    @Mock
    protected MessageForwardingPolicy messageForwardingPolicy;
    
    @Mock
    protected MessageAcceptancePolicy messageAcceptancePolicy;
    
    @Mock
    protected ProducerTemplate producerTemplate;
    
    protected void mockMessageFlowIdCamelMessage() {
        // Mock the camel exchange message to return a mock message.
        when(exchange.getMessage()).thenReturn(camelMessage);
        when(camelMessage.getBody(Long.class)).thenReturn(parentMessageFlowId);
        doNothing().when(camelMessage).setHeader(anyString(), any());
    }
    
    
    protected void mockEventIdCamelMessage() {
        // Mock the camel exchange message to return a mock message.
        when(exchange.getMessage()).thenReturn(camelMessage);
        when(camelMessage.getBody(Long.class)).thenReturn(eventId);
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
        
        when(messageProducer.getMessageForwardingPolicy()).thenReturn(messageForwardingPolicy);
        when(messageForwardingPolicy.applyPolicy(parentMessageFlowDto)).thenReturn(notSuccess);
        
        return notSuccess;
    }

    

    /**
     * A successful forwarding policy.  This means the message is forwarded.
     * 
     * @return
     * @throws ComponentConfigurationException
     * @throws FilterException
     */
    protected MessageFlowPolicyResult mockSuccessMessageForwardingPolicy() throws ComponentConfigurationException, FilterException {
        MessageFlowPolicyResult success = new MessageFlowPolicyResult(true);
        
        when(messageProducer.getMessageForwardingPolicy()).thenReturn(messageForwardingPolicy);
        when(messageForwardingPolicy.applyPolicy(parentMessageFlowDto)).thenReturn(success);
        
        return success;
    }

    
    /**
     * A successful acceptance policy.  This means the message is accepted by the component.
     * 
     * @return
     * @throws ComponentConfigurationException
     * @throws FilterException
     */
    protected MessageFlowPolicyResult mockSuccessMessageAcceptancePolicy() throws ComponentConfigurationException, FilterException {
        MessageFlowPolicyResult success = new MessageFlowPolicyResult(true);
        
        when(messageConsumer.getMessageAcceptancePolicy()).thenReturn(messageAcceptancePolicy);
        when(messageAcceptancePolicy.applyPolicy(parentMessageFlowDto)).thenReturn(success);
        
        return success;
    }

    
    /**
     * A successful acceptance policy.  This means the message is not accepted by the component.
     * 
     * @return
     * @throws ComponentConfigurationException
     * @throws FilterException
     */
    protected MessageFlowPolicyResult mockNotSuccessMessageAcceptancePolicy() throws ComponentConfigurationException, FilterException {
        MessageFlowPolicyResult notSuccess = new MessageFlowPolicyResult(false);
        
        when(messageConsumer.getMessageAcceptancePolicy()).thenReturn(messageAcceptancePolicy);
        when(messageAcceptancePolicy.applyPolicy(parentMessageFlowDto)).thenReturn(notSuccess);
        
        return notSuccess;
    }

    
    protected MessageFlowProcessingException mockRetryableException() {
        Throwable queryTimeOutException = new QueryTimeoutException("Query timed out");
        DataAccessException dataAccessException = new DataAccessException("DB error", queryTimeOutException) {};
        MessageFlowProcessingException ex = new MessageFlowProcessingException("Message flow error", parentMessageFlowId, dataAccessException);
        
        return ex;
    }
    
    
    protected MessageFlowProcessingException mockNotRetryableException() {
        Throwable queryTimeOutException = new DataIntegrityViolationException("Duplicate key");
        DataAccessException dataAccessException = new DataAccessException("DB error", queryTimeOutException) {};
        MessageFlowProcessingException ex = new MessageFlowProcessingException("Message flow error", parentMessageFlowId, dataAccessException);
        
        return ex;
    }

    
    protected void mockRetrieveMessageFlow() throws MessageFlowNotFoundException, MessageFlowProcessingException {
        when(messageFlowService.retrieveMessageFlow(parentMessageFlowId)).thenReturn(parentMessageFlowDto);
        when(parentMessageFlowDto.getId()).thenReturn(parentMessageFlowId);
    }
    
    
    protected void mockDeleteEvent() throws OutboxEventNotFoundException, OutboxEventProcessingException {
        doNothing().when(outboxService).deleteEvent(eventId);
    }
}
