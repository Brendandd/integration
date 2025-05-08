package integration.core.messaging.component.adapter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
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
            return springContext.getBean("forwardAllMessages", MessageForwardingPolicy.class);
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
    public String getMessageForwardingUriString() throws ConfigurationException {
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
    protected Set<Class<? extends Annotation>> getAllowedAnnotations() {
        Set<Class<? extends Annotation>> allowedAnnotations = new LinkedHashSet<>();
        
        allowedAnnotations.add(IntegrationComponent.class);
        allowedAnnotations.add(ForwardingPolicy.class);
        allowedAnnotations.add(AllowedContentType.class);
        allowedAnnotations.add(AdapterOption.class);
        allowedAnnotations.add(AdapterOptions.class);

        return allowedAnnotations;
    }
}

