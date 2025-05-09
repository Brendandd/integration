package integration.core.messaging.component.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;
import integration.core.messaging.component.handler.filter.MessageForwardingPolicy;

/**
 * Base class for all inbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
@ForwardingPolicy(name = "forwardAllMessages")
public abstract class BaseInboundAdapter extends BaseAdapter implements MessageProducer {
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    @Override
    public void addMessageConsumer(MessageConsumer messageConsumer) {
        if (!messageConsumers.contains(messageConsumer)) {
            this.messageConsumers.add(messageConsumer);
            messageConsumer.addMessageProducer(this);
        }
    }

    
    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() {
        ForwardingPolicy annotation = this.getClass().getAnnotation(ForwardingPolicy.class);
               
        if (annotation == null) {
            throw new ConfigurationException("@ForwardingPolicy annotation not found.  It is mandatory for all components");
        }

        return springContext.getBean(annotation.name(), MessageForwardingPolicy.class);
    }

    
    @Override
    protected Long getBodyContent(MessageFlowDto messageFlowDto) {
        return messageFlowDto.getId();
    }

    
    /**
     * Where to get the message from.  This is a Camel URI.
     * 
     * 
     * @return
     */
    public abstract String getFromUriString();

    
    @Override
    public String getMessageForwardingUriString(Exchange exchange) throws ConfigurationException {
        return "jms:topic:VirtualTopic." + getComponentPath();
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();
                
        // Entry point for an inbound adapters outbound message handling. 
        from("direct:outboundMessageHandling-" + getComponentPath())
            .routeId("outboundMessageHandling-" + getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                                               
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowDto);
                                  
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            messagingFlowService.recordMessageFlowEvent(parentMessageFlowDto.getId(), getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                        } else {
                            messagingFlowService.recordMessageNotForwarded(BaseInboundAdapter.this, parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
                        }
                    }
                });
    }

    
    @Override
    protected void configureRequiredAnnotations() {            
        requiredAnnotations.add(ForwardingPolicy.class);
    }

    
    protected void addProperties(Exchange exchange, long messageFlowId) {
        Class<?> clazz = this.getClass();
        
        while (clazz != null) {
            StoreHeader[] headers = clazz.getDeclaredAnnotationsByType(StoreHeader.class);
            for (StoreHeader header : headers) {
                String headerValue = (String)exchange.getMessage().getHeader(header.name());
                messageFlowPropertyService.addProperty(header.name(), headerValue, messageFlowId);
            }
            
            clazz = clazz.getSuperclass();
        }
    }
}

