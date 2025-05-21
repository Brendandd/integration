package integration.core.runtime.messaging.component.type.connector;

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
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.type.connector.annotation.From;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.filter.MessageForwardingPolicy;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.ForwardingPolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;

/**
 * Inbound route connector. Accepts messages from other routes.
 * 
 * @author Brendan Douglas
 */
@ComponentType(type = IntegrationComponentTypeEnum.INBOUND_ROUTE_CONNECTOR)
@ForwardingPolicy(name = "forwardAllMessages")
public abstract class BaseInboundRouteConnector extends BaseRouteConnector implements MessageProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseInboundRouteConnector.class);
    
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    @Override
    public String getMessageForwardingUriString(Exchange exchange) throws ComponentConfigurationException, RouteConfigurationException {
        return "jms:topic:VirtualTopic." + getComponentPath();
    }
    
    
    @Override
    protected Long getBodyContent(MessageFlowDto messageFlowDto) {
        return messageFlowDto.getId();
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
    public void configure() throws Exception {
        super.configure();

        from("jms:VirtualTopic." + getConnectorName() + "::Consumer." + getComponentPath() + ".VirtualTopic." + getName() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("inboundEntryPoint-" + getIdentifier())
            .routeGroup(getComponentPath())
            .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
            .transacted()
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        exchange.getMessage().setHeader(MESSAGE_FLOW_ID, parentMessageFlowId);
                                                              
                        // An inbound route connector always accepts the message form the outbound route connector.
                        MessageFlowDto messageFlowDto  = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowId,MessageFlowActionType.ACCEPTED);
                        
                        messageFlowEventService.recordMessageFlowEvent(messageFlowDto.getId(),getIdentifier(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE);               }
                });
 
        
        // Entry point for an inbound adapters outbound message handling. 
        from("direct:outboundMessageHandling-" + getIdentifier())
            .routeId("outboundMessageHandling-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Record the outbound message.
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        exchange.getMessage().setHeader(MESSAGE_FLOW_ID, parentMessageFlowId);
                        
                        MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                                               
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowDto);
                                                                       
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            MessageFlowDto forwardedMessageFlowDto = messagingFlowService.recordMessageFlow(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.PENDING_FORWARDING);
                            messageFlowEventService.recordMessageFlowEvent(forwardedMessageFlowDto.getId(),getIdentifier(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE);
                        } else {
                            messagingFlowService.recordMessageNotForwarded(getIdentifier(), parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
                        }
                    }
                });
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
