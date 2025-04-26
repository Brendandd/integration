package integration.messaging;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.service.ConfigurationService;
import integration.messaging.component.Component;
import integration.messaging.component.MessageConsumer;
import integration.messaging.component.MessageProducer;
import integration.messaging.component.adapter.BaseInboundAdapter;
import integration.messaging.component.adapter.BaseOutboundAdapter;
import integration.messaging.component.connector.BaseInboundRouteConnector;
import integration.messaging.component.connector.BaseOutboundRouteConnector;
import integration.messaging.component.handler.MessageHandler;

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

    protected String name;

    private List<Component> components = new ArrayList<Component>();

    public BaseRoute(String name) {
        this.name = name;
    }

    
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
        for (MessageConsumer consumer : consumers) {
            producer.addMessageConsumer(consumer);
        } 
    }

    
    /**
     * Associates a component with this route.
     * 
     * @param component
     * @throws Exception
     */
    public void addComponentToRoute(Component component) throws Exception {
        component.setRoute(name);
        components.add(component);
    }

    public abstract void configure() throws Exception;

    public void start() throws Exception {
        for (Component component : components) {
            camelContext.addRoutes(((RouteBuilder) component));
        }

        camelContext.start();
    }
}
