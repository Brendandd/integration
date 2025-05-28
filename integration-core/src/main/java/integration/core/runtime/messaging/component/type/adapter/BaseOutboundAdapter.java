package integration.core.runtime.messaging.component.type.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.MessageFlowPropertyDto;
import integration.core.runtime.messaging.component.EgressQueueConsumerWithoutForwardingPolicyProcessor;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.type.adapter.annotation.InjectHeader;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;
import integration.core.runtime.messaging.component.type.handler.filter.MessageFlowPolicyResult;
import integration.core.runtime.messaging.component.type.handler.filter.annotation.AcceptancePolicy;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all outbound adapters.
 * 
 * @author Brendan Douglas
 *
 */
@AcceptancePolicy(name = "acceptAllMessages")
public abstract class BaseOutboundAdapter extends BaseAdapter implements MessageConsumer  {
    protected List<MessageProducer> messageProducers = new ArrayList<>();
    
    @Autowired
    private EgressQueueConsumerWithoutForwardingPolicyProcessor egressQueueConsumerWithoutForwardingPolicyProcessor;
    
    @PostConstruct
    public void init() {
        egressQueueConsumerWithoutForwardingPolicyProcessor.setComponent(this);
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
        
        // An outbound adapter can consume from multiple topics, one for each producer.
        for (MessageProducer messageProducer : messageProducers) {
            
            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
                .routeId("ingress-" + messageProducer.getComponentPath() + "-" + messageProducer.getComponentPath())
                .routeGroup(getComponentPath())
                .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
                .transacted()
                    .process(exchange -> {
                        MessageFlowDto parentMessageFlowDto = getMessageFlowDtoFromExchangeBody(exchange);
                                                   
                        MessageFlowPolicyResult result = getMessageAcceptancePolicy().applyPolicy(parentMessageFlowDto);
                        if (result.isSuccess()) {
                            MessageFlowDto messageFlowDto = messageFlowService.recordMessageFlowWithSameContent(getIdentifier(), parentMessageFlowDto.getId(), MessageFlowActionType.ACCEPTED);
                        
                            outboxService.recordEvent(messageFlowDto.getId(),getIdentifier(), OutboxEventType.INGRESS_COMPLETE); 
                        } else {
                            messageFlowService.recordMessageNotAccepted(getIdentifier(), parentMessageFlowDto.getId(), result, MessageFlowActionType.NOT_ACCEPTED);
                        }
                    });
        }
    }

    
    @Override
    protected void configureEgressQueueConsumerRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        from("jms:queue:egressQueue-" + getIdentifier())
        .routeId("egressQueue-" + getIdentifier())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(getComponentPath())
            .transacted()
                .process(egressQueueConsumerWithoutForwardingPolicyProcessor);
    }

    
    @Override
    protected void configureRequiredAnnotations() {    
        requiredAnnotations.add(AcceptancePolicy.class);
    }

    
    /**
     * Get the headers which this component wants to load based on the @LoadHeader annotation.
     * 
     * @param messageFlowDto
     * @return
     */
    protected Map<String,Object>getHeaders(MessageFlowDto messageFlowDto) {
        Map<String, Object> headers = new HashMap<>();

        InjectHeader[] loadHeaders = this.getClass().getAnnotationsByType(InjectHeader.class);

        // Collect allowed keys
        Set<String> allowedKeys = new HashSet<>();
        for (InjectHeader header : loadHeaders) {
            allowedKeys.add(header.name());
        }

        for (MessageFlowPropertyDto property : messageFlowDto.getProperties()) {
            if (allowedKeys.contains(property.getKey())) {
                headers.put(property.getKey(), property.getValue());
            }
        }
        
        return headers;
    } 
}
