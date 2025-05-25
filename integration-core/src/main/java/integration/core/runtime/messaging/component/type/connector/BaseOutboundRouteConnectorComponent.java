package integration.core.runtime.messaging.component.type.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.connector.annotation.DynamicDestination;
import integration.core.runtime.messaging.component.type.connector.annotation.StaticDestination;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.AcceptancePolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.exception.retryable.MessageForwardingException;

/**
 * Outbound route connector. Sends messages to other routes.
 * 
 * @author Brendan Douglas
 */
@ComponentType(type = IntegrationComponentTypeEnum.OUTBOUND_ROUTE_CONNECTOR)
@AcceptancePolicy(name = "acceptAllMessages")
public abstract class BaseOutboundRouteConnectorComponent extends BaseRouteConnectorComponent implements MessageConsumer  {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOutboundRouteConnectorComponent.class);
    
    protected List<MessageProducer> messageProducers = new ArrayList<>();

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    protected void forwardMessage(Exchange exchange, MessageFlowDto messageFlowDto, long eventId) throws MessageForwardingException {
        try {
            producerTemplate.sendBody("jms:topic:VirtualTopic." + getConnectorName(exchange), messageFlowDto.getId());
        } catch(Exception e) {
            throw new MessageForwardingException("Error forwarding message out of component", eventId, getIdentifier(), messageFlowDto.getId(), e);
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
    public MessageAcceptancePolicy getMessageAcceptancePolicy() throws ComponentConfigurationException {
        AcceptancePolicy annotation = getRequiredAnnotation(AcceptancePolicy.class);
        
        return springContext.getBean(annotation.name(), MessageAcceptancePolicy.class);
    }
    
    
    @Override
    protected void configureIngressRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        // Creates one or more routes based on this components source components.  Each route reads from a topic. This is the entry point for outbound route connectors.
        for (MessageProducer messageProducer : messageProducers) {
          
            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
                .routeId("ingress-" + getIdentifier())
                .routeGroup(getComponentPath())
                .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
                .transacted()
                    .process(exchange -> {
                        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
                        
                        // Determine if the message should be accepted by this route connector.
                        MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(parentMessageFlowDto);
                        
                        if (result.isSuccess()) {
                            MessageFlowDto messageFlowDto = messageFlowService.recordMessageFlowWithSameContent(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.ACCEPTED);
                        
                            // Record an event so the message can be forwarded to other components for processing.
                            outboxService.recordEvent(messageFlowDto.getId(),getIdentifier(), OutboxEventType.INGRESS_COMPLETE);
                        } else {
                            messageFlowService.recordMessageNotAccepted(getIdentifier(), parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_ACCEPTED);
                        }
                    });
        }  
    }

    
    @Override
    protected void configureEgressQueueConsumerRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        // Entry point for a outbound route connector outbound message handling. 
        from("jms:queue:egressQueue-" + getIdentifier())
        .routeId("egressQueue-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .transacted()
            
                .process(exchange -> {          
                    MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
                    
                    MessageFlowDto forwardedMessageFlowDto = messageFlowService.recordMessageFlowWithSameContent(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.PENDING_FORWARDING);
                    outboxService.recordEvent(forwardedMessageFlowDto.getId(), getIdentifier(),OutboxEventType.PENDING_FORWARDING); 
                });        
    }

    
    /**
     * Get the connector name which can either be a static destination defined using @StaticDestination or a dynamic destination using @DynamicDestination.
     * If the destination is dynamic then a DynamicDesintationResolver needs to be provided.
     * 
     * @param exchange
     * @return
     * @throws ComponentConfigurationException 
     */
    private String getConnectorName(Exchange exchange) throws ComponentConfigurationException {
        StaticDestination staticAnnotation = this.getClass().getAnnotation(StaticDestination.class);
        DynamicDestination dynamicAnnotation = this.getClass().getAnnotation(DynamicDestination.class);
        
        if (staticAnnotation != null && dynamicAnnotation != null) {
            throw new ComponentConfigurationException("Both @StaticDestination and @DynamicDestination annotations found.  One one is allowed.", getIdentifier());
        }
        
        if (staticAnnotation == null && dynamicAnnotation == null) {
            throw new ComponentConfigurationException("Neither @StaticDestination and @DynamicDestination annotations found.  One is required.", getIdentifier());
        }
        
        if (staticAnnotation != null) {
            return staticAnnotation.connectorName();
        }
        
        // If we get here the component has a dynamic destination so we must get the resolver bean and call its resolveDestination method.
        DynamicDestinationResolver resolver = springContext.getBean(dynamicAnnotation.destinationResolver(), DynamicDestinationResolver.class);
        return resolver.resolveDestination(exchange);
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        requiredAnnotations.add(StaticDestination.class);
        requiredAnnotations.add(DynamicDestination.class);
        requiredAnnotations.add(AcceptancePolicy.class);
    }
}
