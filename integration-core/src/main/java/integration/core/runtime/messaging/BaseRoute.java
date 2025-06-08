package integration.core.runtime.messaging;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import integration.core.runtime.messaging.component.MessageConsumer;
import integration.core.runtime.messaging.component.MessageProducer;
import integration.core.runtime.messaging.component.MessagingComponent;
import integration.core.runtime.messaging.component.type.adapter.BaseInboundAdapter;
import integration.core.runtime.messaging.component.type.adapter.BaseOutboundAdapter;
import integration.core.runtime.messaging.component.type.connector.BaseInboundRouteConnectorComponent;
import integration.core.runtime.messaging.component.type.connector.BaseOutboundRouteConnectorComponent;
import integration.core.runtime.messaging.component.type.handler.BaseMessageHandlerComponent;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.service.OutboxService;
import integration.core.service.StartupService;

/**
 * Base class for all routes. A route is composed of components and determines
 * the message flow within a system.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseRoute {
    
    @Autowired
    protected CamelContext camelContext;

    @Autowired
    protected StartupService routeConfigurationService;
        
    protected long identifier;
    
    private final List<MessagingComponent> components = new ArrayList<>();
    
    @Autowired
    protected ProducerTemplate producerTemplate;
    
    @Autowired
    protected OutboxService outboxService;
    
    @Autowired
    protected Environment env;
   
    /**
     * An inbound adapter message can be sent to one or more message processors.
     * 
     * @param producer
     * @param consumers
     */
    public void addInboundFlow(BaseInboundAdapter producer, BaseMessageHandlerComponent ... consumers) {
        addFlow(producer, consumers);         
    }

    
    /**
     * An inbound router connector message can be sent to one or more message consumers.
     *
     * @param producer
     * @param consumers
     */
    public void addInboundFlow(BaseInboundRouteConnectorComponent producer, BaseMessageHandlerComponent ... consumers) {
        addFlow(producer, consumers);          
    }

    
    /**
     * Direct flow from inbound adapter to outbound adapter.
     * 
     * @param producer
     * @param consumers
     */
    public void addDirectFlow(BaseInboundAdapter producer, BaseOutboundAdapter ... consumers) {
        addFlow(producer, consumers); 
    }

    
    /**
     * Direct flow from inbound adapter to outbound route connector.
     * 
     * @param producer
     * @param consumers
     */
    public void addDirectFlow(BaseInboundAdapter producer, BaseOutboundRouteConnectorComponent consumers) {
        addFlow(producer, consumers); 
    }

    
    /**
     * Direct flow from inbound route connector to outbound route connector.
     * 
     * @param producer
     * @param consumers
     */
    public void addDirectFlow(BaseInboundRouteConnectorComponent producer, BaseOutboundRouteConnectorComponent consumers) {
        addFlow(producer, consumers); 
    }

    
    /**
     * Direct flow from inbound route connector to outbound route connector.
     * 
     * @param producer
     * @param consumers
     */
    public void addDirectFlow(BaseInboundRouteConnectorComponent producer, BaseOutboundAdapter consumers) {
        addFlow(producer, consumers); 
    }

    
    /**
     * An message processor message can be sent to one or more message consumers. 
     *
     * @param producer
     * @param consumers
     */
    public void addInternalFlow(BaseMessageHandlerComponent producer, BaseMessageHandlerComponent ... consumers) {
        addFlow(producer, consumers);         
    } 

    
    public void addOutboundFlow(BaseMessageHandlerComponent producer, BaseOutboundAdapter ... consumers) {
        addFlow(producer, consumers);       
    } 

    
    public void addOutboundFlow(BaseMessageHandlerComponent producer, BaseOutboundRouteConnectorComponent ... consumers) {
        addFlow(producer, consumers);       
    } 

    
    private void addFlow(MessageProducer producer, MessageConsumer ... consumers) {
        if (!this.components.contains(producer)) {
            components.add(producer);
        }
        
        for (MessageConsumer consumer : consumers) {
            if (!this.components.contains(consumer)) {
                components.add(consumer);
            }
            
            producer.addMessageConsumer(consumer);
        } 
    }

    
    public abstract void configureRoute() throws Exception;
    
    
    protected void applyConfiguration() throws Exception { 
        routeConfigurationService.configureRoute(this, components);

        for (MessagingComponent component : components) {
            camelContext.addRoutes((RoutesBuilder)component);
        }
    }

    
    public String getName() throws RouteConfigurationException {
        IntegrationRoute annotation = this.getClass().getAnnotation(IntegrationRoute.class);
        
        if (annotation == null) {
            throw new RouteConfigurationException("@IntegrationRoute annotation not found.  It is mandatory for all routes", getIdentifier());
        }
        
        return annotation.name();
    }

    
    public long getIdentifier() {
        return identifier;
    }

    
    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }
}
