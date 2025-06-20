package integration.core.runtime.messaging.component.type.adapter.outbound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.MessageFlowPropertyDto;
import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.WriteToInboxProcessor;
import integration.core.runtime.messaging.component.type.adapter.BaseAdapter;
import integration.core.runtime.messaging.component.type.adapter.annotation.InjectHeader;
import integration.core.runtime.messaging.component.type.handler.filter.MessageAcceptancePolicy;
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
    protected final List<MessageProducer> messageProducers = new ArrayList<>();
    
    
    @Autowired
    protected WriteToInboxProcessor writeToInboxProcessor;
    
    @PostConstruct
    public void BaseOutboundAdapterInit() {
        writeToInboxProcessor.setComponent(this);
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
        
        for (MessageProducer messageProducer : messageProducers) {
            from("jms:VirtualTopic." + messageProducer.getComponentPath() + "::Consumer." + getComponentPath() + ".VirtualTopic." + messageProducer.getComponentPath() + "?concurrentConsumers=5&maxConcurrentConsumers=10")
            .routeId("ingress-" + getIdentifier() + "-" + messageProducer.getIdentifier())
            .routeGroup(getComponentPath())
            .autoStartup(inboundState == IntegrationComponentStateEnum.RUNNING)
            .transacted("jmsTransactionPolicy")
            .process(writeToInboxProcessor);
        } 
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
    public Map<String,Object>getHeaders(MessageFlowDto messageFlowDto) {
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
