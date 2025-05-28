package integration.core.runtime.messaging.component.type.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.dto.MessageFlowDto;
import integration.core.runtime.messaging.component.EgressQueueConsumerWithForwardingPolicyProcessor;
import integration.core.runtime.messaging.component.IngressTopicConsumerWithoutAcceptancePolicyProcessor;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.connector.annotation.From;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.ForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.exception.retryable.MessageForwardingException;
import jakarta.annotation.PostConstruct;

/**
 * Inbound route connector. Accepts messages from other routes.
 * 
 * @author Brendan Douglas
 */
@ComponentType(type = IntegrationComponentTypeEnum.INBOUND_ROUTE_CONNECTOR)
@ForwardingPolicy(name = "forwardAllMessages")
public abstract class BaseInboundRouteConnectorComponent extends BaseRouteConnectorComponent implements MessageProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseInboundRouteConnectorComponent.class);
    
    @Autowired
    private EgressQueueConsumerWithForwardingPolicyProcessor egressQueueConsumerWithForwardingPolicyProcessor;
    
    @Autowired
    IngressTopicConsumerWithoutAcceptancePolicyProcessor ingressTopicConsumerWithoutAcceptancePolicyProcessor;
        
    @PostConstruct
    public void init() {
        egressQueueConsumerWithForwardingPolicyProcessor.setComponent(this);
        ingressTopicConsumerWithoutAcceptancePolicyProcessor.setComponent(this);
    }
    
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    
    @Override
    protected void forwardMessage(Exchange exchange, MessageFlowDto messageFlowDto, long eventId) throws MessageForwardingException {
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
    public MessageForwardingPolicy getMessageForwardingPolicy() throws ComponentConfigurationException {
        ForwardingPolicy annotation = getRequiredAnnotation(ForwardingPolicy.class);

        return springContext.getBean(annotation.name(), MessageForwardingPolicy.class);
    }

    
    @Override
    protected void configureIngressRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        from("jms:VirtualTopic." + getConnectorName() + "::Consumer." + getComponentPath() + ".VirtualTopic." + getName() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
        .routeId("ingress-" + getIdentifier())
        .routeGroup(getComponentPath())
        .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
        .transacted()
            .process(ingressTopicConsumerWithoutAcceptancePolicyProcessor);
    }

    
    
    @Override
    protected void configureEgressQueueConsumerRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        from("jms:queue:egressQueue-" + getIdentifier())
        .routeId("egressQueue-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .transacted()
                .process(egressQueueConsumerWithForwardingPolicyProcessor);
    }

    
    public String getConnectorName() throws ComponentConfigurationException {
        From annotation = getRequiredAnnotation(From.class);
                
        return annotation.connectorName();
    }

    
    @Override
    protected void configureRequiredAnnotations() {        
        requiredAnnotations.add(From.class);
        requiredAnnotations.add(ForwardingPolicy.class);
    }   
}
