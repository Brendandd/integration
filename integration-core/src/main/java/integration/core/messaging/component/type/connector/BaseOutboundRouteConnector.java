package integration.core.messaging.component.type.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.exception.ConfigurationException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.annotation.ComponentType;
import integration.core.messaging.component.type.connector.annotation.DynamicDestination;
import integration.core.messaging.component.type.connector.annotation.StaticDestination;
import integration.core.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.messaging.component.type.handler.filter.annotation.AcceptancePolicy;

/**
 * Outbound route connector. Sends messages to other routes.
 * 
 * @author Brendan Douglas
 */
@ComponentType(type = IntegrationComponentTypeEnum.OUTBOUND_ROUTE_CONNECTOR)
@AcceptancePolicy(name = "acceptAllMessages")
public abstract class BaseOutboundRouteConnector extends BaseRouteConnector implements MessageConsumer  {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOutboundRouteConnector.class);
    
    protected List<MessageProducer> messageProducers = new ArrayList<>();

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    public String getMessageForwardingUriString(Exchange exchange) throws ConfigurationException {
        return "jms:topic:VirtualTopic." + getConnectorName(exchange);
    }

    
    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }

    
    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() throws ConfigurationException {
        AcceptancePolicy annotation = getRequiredAnnotation(AcceptancePolicy.class);
        
        return springContext.getBean(annotation.name(), MessageAcceptancePolicy.class);
    }
    
    
    @Override
    protected Long getBodyContent(MessageFlowDto messageFlowDto) {
        return messageFlowDto.getId();
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();

       
        // Creates one or more routes based on this components source components.  Each route reads from a topic. This is the entry point for outbound route connectors.
        for (MessageProducer messageProducer : messageProducers) {
          
            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
                .routeId("inboundEntryPoint-" + getIdentifier())
                .routeGroup(getComponentPath())
                .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
                .transacted()
                    .process(new Processor() {
                    
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            // Retrieve the inbound message.
                            long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                                                       
                            MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                            
                            // Determine if the message should be accepted by this route connector.
                            MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(parentMessageFlowDto);
                            
                            if (result.isSuccess()) {
                                MessageFlowDto messageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowId, MessageFlowActionType.ACCEPTED);
                            
                                // Record an event so the message can be forwarded to other components for processing.
                                messagingFlowService.recordMessageFlowEvent(messageFlowDto.getId(),getIdentifier(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE);
                            } else {
                                messagingFlowService.recordMessageNotAccepted(getIdentifier(), parentMessageFlowId, result, MessageFlowActionType.NOT_ACCEPTED);
                            }
                        }
                    });
        }

        
        // Entry point for a outbound route connector outbound message handling. 
        from("direct:outboundMessageHandling-" + getIdentifier())
            .routeId("outboundMessageHandling-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {               
                        // Record the outbound message.
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        
                        MessageFlowDto forwardedMessageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowId, MessageFlowActionType.PENDING_FORWARDING);
                        messagingFlowService.recordMessageFlowEvent(forwardedMessageFlowDto.getId(), getIdentifier(),MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }

    
    /**
     * Get the connector name which can either be a static destination defined using @StaticDestination or a dynamic destination using @DynamicDestination.
     * If the destination is dynamic then a DynamicDesintationResolver needs to be provided.
     * 
     * @param exchange
     * @return
     * @throws ConfigurationException
     */
    public String getConnectorName(Exchange exchange) throws ConfigurationException {
        StaticDestination staticAnnotation = this.getClass().getAnnotation(StaticDestination.class);
        DynamicDestination dynamicAnnotation = this.getClass().getAnnotation(DynamicDestination.class);
        
        if (staticAnnotation != null && dynamicAnnotation != null) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, getIdentifier()));
            throw new ConfigurationException("Both @StaticDestination and @DynamicDestination annotations found.  One one is allowed.", identifiers, false);
        }
        
        if (staticAnnotation == null && dynamicAnnotation == null) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, getIdentifier()));
            throw new ConfigurationException("Neither @StaticDestination and @DynamicDestination annotations found.  One is required.", identifiers, false);
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
