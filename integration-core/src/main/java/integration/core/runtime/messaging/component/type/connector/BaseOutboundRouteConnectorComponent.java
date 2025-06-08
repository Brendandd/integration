package integration.core.runtime.messaging.component.type.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.runtime.messaging.component.EgressQueueConsumerWithoutForwardingPolicyProcessor;
import integration.core.runtime.messaging.component.IngressTopicConsumerWithAcceptancePolicyProcessor;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.connector.annotation.DynamicDestination;
import integration.core.runtime.messaging.component.type.connector.annotation.StaticDestination;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.AcceptancePolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;

/**
 * Outbound route connector. Sends messages to other routes.
 * 
 * @author Brendan Douglas
 */
@ComponentType(type = IntegrationComponentTypeEnum.OUTBOUND_ROUTE_CONNECTOR)
@AcceptancePolicy(name = "acceptAllMessages")
public abstract class BaseOutboundRouteConnectorComponent extends BaseRouteConnectorComponent implements MessageConsumer  {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOutboundRouteConnectorComponent.class);
    
    protected final List<MessageProducer> messageProducers = new ArrayList<>();
    
    @Autowired
    private EgressQueueConsumerWithoutForwardingPolicyProcessor egressQueueConsumerWithoutForwardingPolicyProcessor;
    
    @Autowired
    protected IngressTopicConsumerWithAcceptancePolicyProcessor ingressTopicConsumerWithAcceptancePolicyProcessor;
    
    @Autowired
    protected OutboundRouteConnectorForwardingProcessor outboundRouteConnectorForwardingProcessor;
    
    @PostConstruct
    public void BaseOutboundRouteConnectorComponentInit() {
        egressQueueConsumerWithoutForwardingPolicyProcessor.setComponent(this);
        ingressTopicConsumerWithAcceptancePolicyProcessor.setComponent(this);
        outboundRouteConnectorForwardingProcessor.setComponent(this);
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
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
        
        // The entry point for all outbound route connectors.  Consumes from one or more topics.
        for (MessageProducer messageProducer : messageProducers) {
          
            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?acknowledgementModeName=SESSION_TRANSACTED&concurrentConsumers=10&maxConcurrentConsumers=30")
                .routeId("ingress-" + getIdentifier())
                .routeGroup(getComponentPath())
                .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
                .transacted()
                    .process(ingressTopicConsumerWithAcceptancePolicyProcessor);
        }  
    }

    
    @Override
    protected EgressQueueConsumerWithoutForwardingPolicyProcessor getEgressQueueConsumerProcessor() throws ComponentConfigurationException, RouteConfigurationException {
        return egressQueueConsumerWithoutForwardingPolicyProcessor;
    }
    
    
    @Override
    protected OutboundRouteConnectorForwardingProcessor getEgressForwardingProcessor() throws ComponentConfigurationException, RouteConfigurationException {
        return outboundRouteConnectorForwardingProcessor;
    }

    
    /**
     * Get the connector name which can either be a static destination defined using @StaticDestination or a dynamic destination using @DynamicDestination.
     * If the destination is dynamic then a DynamicDesintationResolver needs to be provided.
     * 
     * @param exchange
     * @return
     * @throws ComponentConfigurationException 
     */
    public String getConnectorName(Exchange exchange) throws ComponentConfigurationException {
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
