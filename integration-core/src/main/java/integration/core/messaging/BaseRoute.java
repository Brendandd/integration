package integration.core.messaging;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.exception.ConfigurationException;
import integration.core.messaging.component.MessageConsumer;
import integration.core.messaging.component.MessageProducer;
import integration.core.messaging.component.MessagingComponent;
import integration.core.messaging.component.adapter.BaseInboundAdapter;
import integration.core.messaging.component.adapter.BaseOutboundAdapter;
import integration.core.messaging.component.connector.BaseInboundRouteConnector;
import integration.core.messaging.component.connector.BaseOutboundRouteConnector;
import integration.core.messaging.component.handler.MessageHandler;
import integration.core.service.ConfigurationService;

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
    protected ConfigurationService configurationService;
        
    protected long identifier;
    
    private List<MessagingComponent> components = new ArrayList<>();
   
    /**
     * An inbound adapter message can be sent to one or more message processors.
     * 
     * @param messageProducer
     * @param messageConsumers
     */
    public void addInboundFlow(BaseInboundAdapter producer, MessageHandler ... consumers) {
        addFlow(producer, consumers);         
    }

    
    /**
     * An inbound router connector message can be sent to one or more message consumers.
     * 
     * @param messageProducer
     * @param messageConsumers
     */
    public void addInboundFlow(BaseInboundRouteConnector producer, MessageHandler ... consumers) {
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
    public void addDirectFlow(BaseInboundAdapter producer, BaseOutboundRouteConnector consumers) {
        addFlow(producer, consumers); 
    }

    
    /**
     * Direct flow from inbound route connector to outbound route connector.
     * 
     * @param producer
     * @param consumers
     */
    public void addDirectFlow(BaseInboundRouteConnector producer, BaseOutboundRouteConnector consumers) {
        addFlow(producer, consumers); 
    }

    
    /**
     * Direct flow from inbound route connector to outbound route connector.
     * 
     * @param producer
     * @param consumers
     */
    public void addDirectFlow(BaseInboundRouteConnector producer, BaseOutboundAdapter consumers) {
        addFlow(producer, consumers); 
    }

    
    /**
     * An message processor message can be sent to one or more message consumers. 
     * 
     * @param messageProducer
     * @param messageConsumers
     */
    public void addInternalFlow(MessageHandler producer, MessageHandler ... consumers) {
        addFlow(producer, consumers);         
    } 

    
    public void addOutboundFlow(MessageHandler producer, BaseOutboundAdapter ... consumers) {
        addFlow(producer, consumers);       
    } 

    
    public void addOutboundFlow(MessageHandler producer, BaseOutboundRouteConnector ... consumers) {
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

    public void applyConfiguration() throws Exception { 
        configurationService.configureRoute(this, components);

        for (MessagingComponent component : components) {
            camelContext.addRoutes((RoutesBuilder)component);
        }
    }

    
    public String getName() {
        IntegrationRoute annotation = this.getClass().getAnnotation(IntegrationRoute.class);
        
        if (annotation == null) {
            throw new ConfigurationException("@IntegrationRoute annotation not found.  It is mandatory for all routes");
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
