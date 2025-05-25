package integration.core.runtime.messaging.component.type.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.BaseMessagingComponent;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.AcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.ForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.exception.retryable.MessageForwardingException;

/**
 * Base class for all message handlers. A message handler is a component which is not an inbound or outbound adapter.
 * 
 * A processing step allows both the incoming and outgoing messages to be
 * filtered. By default all messages are allowed to be processed.
 * 
 * 
 * @author Brendan Douglas
 *
 */
@AcceptancePolicy(name = "acceptAllMessages")
@ForwardingPolicy(name = "forwardAllMessages")
public abstract class BaseMessageHandlerComponent extends BaseMessagingComponent implements MessageConsumer, MessageProducer {
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();
    protected List<MessageProducer> messageProducers = new ArrayList<>();
    
    
    @Override
    public void forwardMessage(Exchange exchange, MessageFlowDto messageFlowDto, long eventId) throws MessageForwardingException {
        try {
            producerTemplate.sendBody("jms:topic:VirtualTopic." + getComponentPath(), messageFlowDto.getId());
        } catch(Exception e) {
            throw new MessageForwardingException("Error forwarding message out of component", eventId, getIdentifier(), messageFlowDto.getId(), e);
        }
    }
    
    
    @Override
    public void addMessageConsumer(MessageConsumer messageConsumer) {
        if (!messageConsumers.contains(messageConsumer)) {
            this.messageConsumers.add(messageConsumer);
            messageConsumer.addMessageProducer(this);
        }
    }

    
    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }

    
    @Override
    public MessageForwardingPolicy getMessageForwardingPolicy() throws ComponentConfigurationException {
        ForwardingPolicy annotation = getRequiredAnnotation(ForwardingPolicy.class);
          
        return springContext.getBean(annotation.name(), MessageForwardingPolicy.class);
    }

    
    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() throws ComponentConfigurationException {
        AcceptancePolicy annotation = getRequiredAnnotation(AcceptancePolicy.class);
               
        return springContext.getBean(annotation.name(), MessageAcceptancePolicy.class);
    }    
    
    
    @Override
    public void configureIngressRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        // Creates one or more routes based on this components source components.  Each route reads from a topic. This is the entry point for outbound route connectors.
        for (MessageProducer messageProducer : messageProducers) {

            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
                .routeId("ingress-" + getIdentifier() + "-" + messageProducer.getComponentPath())
                .routeGroup(getComponentPath())
                .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
                .transacted()
                    .process(exchange -> {
                        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
                                                        
                        MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(parentMessageFlowDto);
                        if (result.isSuccess()) {
                            // Record the content received by this component.
                            MessageFlowDto acceptedMessageFlowDto = messageFlowService.recordMessageFlowWithSameContent(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.ACCEPTED);
                        
                            // Record an event so the message can be forwarded to other components for processing.
                            outboxService.recordEvent(acceptedMessageFlowDto.getId(),getIdentifier(), OutboxEventType.INGRESS_COMPLETE); 
                        } else {
                            messageFlowService.recordMessageNotAccepted(getIdentifier(), parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_ACCEPTED);
                        }
                    });

        }  
    }

    
    @Override
    public void configureEgressQueueConsumerRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        
        // Entry point for an inbound adapters outbound message handling. 
        from("jms:queue:egressQueue-" + getIdentifier())
        .routeId("egressQueue-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .transacted()
            
                .process(exchange -> {                    
                    MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
                                           
                    MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowDto);
                                                                   
                    // Apply the message forwarding rules and either write an event for further processing or filter the message.
                    if (result.isSuccess()) {
                        MessageFlowDto forwardedMessageFlowDto = messageFlowService.recordMessageFlowWithSameContent(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.PENDING_FORWARDING);
                        outboxService.recordEvent(forwardedMessageFlowDto.getId(),getIdentifier(), OutboxEventType.PENDING_FORWARDING);
                    } else {
                        messageFlowService.recordMessageNotForwarded(getIdentifier(), parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
                    }
                });
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        requiredAnnotations.add(AcceptancePolicy.class);
        requiredAnnotations.add(ForwardingPolicy.class);
    }
}
