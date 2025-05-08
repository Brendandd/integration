package integration.core.messaging.component.connector;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentState;
import integration.core.domain.configuration.ComponentType;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;
import integration.core.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Outbound route connector. Sends messages to other routes.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseOutboundRouteConnector extends BaseRouteConnector implements MessageConsumer  {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOutboundRouteConnector.class);
    
    protected List<MessageProducer> messageProducers = new ArrayList<>();

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    
    @Override
    public ComponentType getType() {
        return ComponentType.OUTBOUND_ROUTE_CONNECTOR;
    }

    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.OUTBOUND_ROUTE_CONNECTOR;
    }
    
    
    @Override
    public String getMessageForwardingUriString() {
        return "jms:topic:VirtualTopic." + getConnectorName();
    }

    
    @Override
    public void addMessageProducer(MessageProducer messageProducer) {
        if (!messageProducers.contains(messageProducer)) {
            this.messageProducers.add(messageProducer);
            messageProducer.addMessageConsumer(this);
        }
    }

    
    @Override
    public MessageAcceptancePolicy getMessageAcceptancePolicy() {
        AcceptancePolicy annotation = this.getClass().getAnnotation(AcceptancePolicy.class);
               
        if (annotation == null) {
            return springContext.getBean("acedptAllMessages", MessageAcceptancePolicy.class);
        }
        
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
                .routeId("inboundEntryPoint-" + getComponentPath())
                .routeGroup(getComponentPath())
                .autoStartup(inboundState == ComponentState.RUNNING)
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
                                MessageFlowDto messageFlowDto = messagingFlowService.recordMessageFlow(BaseOutboundRouteConnector.this, parentMessageFlowId, MessageFlowActionType.ACCEPTED);
                            
                                // Record an event so the message can be forwarded to other components for processing.
                                messagingFlowService.recordMessageFlowEvent(messageFlowDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE);
                            } else {
                                messagingFlowService.recordMessageNotAccepted(BaseOutboundRouteConnector.this, parentMessageFlowId, result, MessageFlowActionType.NOT_ACCEPTED);
                            }
                        }
                    });
        }

        
        // Entry point for a outbound route connector outbound message handling. 
        from("direct:outboundMessageHandling-" + getComponentPath())
            .routeId("outboundMessageHandling-" + getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {               
                        // Record the outbound message.
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        messagingFlowService.recordMessageFlowEvent(parentMessageFlowId, getComponentPath(), getOwner(),MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE); 
                    }
                });
    }

    
    /**
     * 
     */
    @Override
    public String getConnectorName() throws ConfigurationException {
        StaticDestination staticAnnotation = this.getClass().getAnnotation(StaticDestination.class);
        DynamicDestination dynamicAnnotation = this.getClass().getAnnotation(DynamicDestination.class);
        
        if (staticAnnotation != null && dynamicAnnotation != null) {
            throw new ConfigurationException("Both @ToStatic and @ToDynamic annotations found.  One one is allowed.");
        }
        
        if (staticAnnotation == null && dynamicAnnotation == null) {
            throw new ConfigurationException("Neither @ToStatic and @ToDynamic annotations found.  One is required.");
        }
        
        if (staticAnnotation != null) {
            return staticAnnotation.connectorName();
        }
        
        throw new NotImplementedException("Dynamic connector names not implemented yet");
    }

    
    @Override
    protected Set<Class<? extends Annotation>> getAllowedAnnotations() {
        Set<Class<? extends Annotation>> allowedAnnotations = new LinkedHashSet<>();
        
        allowedAnnotations.add(IntegrationComponent.class);
        allowedAnnotations.add(StaticDestination.class);
        allowedAnnotations.add(AcceptancePolicy.class);
        allowedAnnotations.add(AllowedContentType.class);

        return allowedAnnotations;
    }
}
