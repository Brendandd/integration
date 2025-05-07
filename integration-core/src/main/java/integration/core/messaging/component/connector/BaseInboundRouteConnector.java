package integration.core.messaging.component.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import integration.core.domain.configuration.ComponentCategory;
import integration.core.domain.configuration.ComponentState;
import integration.core.domain.configuration.ComponentType;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.handler.filter.MessageFlowPolicyResult;

/**
 * Inbound route connector. Accepts messages from other routes.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseInboundRouteConnector extends BaseRouteConnector implements MessageProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseInboundRouteConnector.class);
    
    protected List<MessageConsumer> messageConsumers = new ArrayList<>();

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
    
    @Override
    public String getMessageForwardingUriString() {
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
    public ComponentType getType() {
        return ComponentType.INBOUND_ROUTE_CONNECTOR;
    }

    @Override
    public ComponentCategory getCategory() {
        return ComponentCategory.INBOUND_ROUTE_CONNECTOR;
    }

    
    @Override
    public void configure() throws Exception {
        super.configure();

        from("jms:VirtualTopic." + getConnectorName() + "::Consumer." + getComponentPath() + ".VirtualTopic." + getName() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("inboundEntryPoint-" + getComponentPath())
            .routeGroup(getComponentPath())
            .autoStartup(inboundState == ComponentState.RUNNING)
            .transacted()
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                                                              
                        // An inbound route connector always accepts the message form the outbound route connector.
                        MessageFlowDto messageFlowDto  = messagingFlowService.recordMessageFlow(BaseInboundRouteConnector.this, parentMessageFlowId,MessageFlowActionType.ACCEPTED);
                        
                        messagingFlowService.recordMessageFlowEvent(messageFlowDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE);               }
                });
 
        
        // Entry point for an inbound adapters outbound message handling. 
        from("direct:outboundMessageHandling-" + getComponentPath())
            .routeId("outboundMessageHandling-" + getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            
                .process(new Processor() {
                    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        // Record the outbound message.
                        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
                        MessageFlowDto parentMessageFlowDto = messagingFlowService.retrieveMessageFlow(parentMessageFlowId);
                                               
                        MessageFlowPolicyResult result = getMessageForwardingPolicy().applyPolicy(parentMessageFlowDto);
                                                                       
                        // Apply the message forwarding rules and either write an event for further processing or filter the message.
                        if (result.isSuccess()) {
                            messagingFlowService.recordMessageFlowEvent(parentMessageFlowDto.getId(),getComponentPath(), getOwner(), MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE);
                        } else {
                            messagingFlowService.recordMessageNotForwarded(BaseInboundRouteConnector.this, parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_FORWARDED);
                        }
                    }
                });
    }
}
