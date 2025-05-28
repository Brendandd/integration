package integration.core.runtime.messaging.component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import integration.core.domain.IdentifierType;
import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.configuration.IntegrationComponentCategoryEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.OutboxEventType;
import integration.core.dto.ComponentDto;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.OutboxEventDto;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.IntegrationException;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.MessageFlowNotFoundException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.exception.retryable.MessageForwardingException;
import integration.core.runtime.messaging.exception.retryable.QueuePublishingException;
import integration.core.runtime.messaging.service.MessageFlowPropertyService;
import integration.core.runtime.messaging.service.MessageFlowService;
import integration.core.runtime.messaging.service.OutboxService;
import integration.core.service.ComponentService;
import jakarta.annotation.PostConstruct;

/**
 * Base class for all Apache Camel messaging component routes.
 * 
 * All messaging components are implemented as multiple Apache Camel routes.     
 * 
 * Communication between different message components is done via JMS topics.  
 * 
 * Messaging components can be adapters (inbound and outbound), message handlers (eg. transformers, filters, splitters) and route connectors (inbound and outbound).
 * 
 * To ensure guaranteed message delivery between components the transactional outbox pattern is used.  Firstly a message is written to an event table within the same transactions as the 
 * message flow record is stored in the main table.  A timer process then processes these events and within the same transaction the event is removed and a message written to a JMS topic to be 
 * picked up by another component.  
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMessagingComponent extends RouteBuilder implements MessagingComponent {   
    
    // Outbox event to route mapping.
    protected final Map<OutboxEventType, String> eventRoutingMap = new HashMap<>();
    
    protected long identifier;
    protected BaseRoute route;
    
    protected IntegrationComponentStateEnum inboundState;
    protected IntegrationComponentStateEnum outboundState;
    
    @Autowired
    protected ComponentService componentConfigurationService;
    
    @Autowired
    protected EgressQueueProducerProcessor egressQueueProducerProcessor;
    
    @Autowired
    protected CamelContext camelContext;
    
    @Autowired
    protected ApplicationContext springContext;
    
    @Autowired
    protected MessageFlowService messageFlowService;
    
    @Autowired
    protected OutboxService outboxService;
    
    protected Map<String,String>componentProperties;

    @Autowired
    protected Ignite ignite;
    
    @Autowired
    protected Environment env;
    
    @Autowired
    protected ProducerTemplate producerTemplate;
    
    @Autowired
    protected MessageFlowPropertyService messageFlowPropertyService;
    
    protected Set<Class<? extends Annotation>> requiredAnnotations = new LinkedHashSet<>();

    public abstract Logger getLogger();
    
    public BaseMessagingComponent() {
        configureEventRouting();
    }
    
    
    @PostConstruct
    public void BaseMessagingComponentInit() {
        egressQueueProducerProcessor.setComponent(this);
    }

    
    /**
     * Makes sure each component has the mandatory annotations for its type.
     * @throws ComponentConfigurationException 
     */
    @Override
    public void validateAnnotations() throws ComponentConfigurationException {
        // These are common for all components.
        requiredAnnotations.add(IntegrationComponent.class);
        requiredAnnotations.add(ComponentType.class);
        requiredAnnotations.add(AllowedContentType.class);
        
        // Collect all annotations in the class hierarchy
        Set<Class<? extends Annotation>> foundAnnotations = new LinkedHashSet<>();

        Class<?> currentClass = this.getClass();
        while (currentClass != null && currentClass != Object.class) {
            for (Annotation annotation : currentClass.getDeclaredAnnotations()) {
                foundAnnotations.add(annotation.annotationType());
            }
            currentClass = currentClass.getSuperclass();
        }

        // Check if all required annotations are present
        for (Class<? extends Annotation> required : requiredAnnotations) {
            if (!foundAnnotations.contains(required)) {
                throw new ComponentConfigurationException("Missing required annotation @" + required.getSimpleName() + " on class " + this.getClass().getName() + " or its hierarchy.", getIdentifier());
            }
        }
        
        getLogger().info("All required annotations found on component: {}", getIdentifier());
    } 

    
    protected abstract void configureRequiredAnnotations();
    
    
    /**
     * The full component path.  route-path
     * @throws ComponentConfigurationException 
     * @throws RouteConfigurationException 
     */
    @Override
    public String getComponentPath() throws ComponentConfigurationException, RouteConfigurationException {
        return route.getName() + "-" + getName();
    }

    
    @Override
    public long getIdentifier() {
        return identifier;
    }

    
    @Override
    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    
    /**
     * Configure global exception handlers which apply to all components.
     * 
     * @param routeBuilder
     */
    protected void configureGlobalExceptionHandlers(RouteBuilder routeBuilder) {
        onException(MessageFlowProcessingException.class)
        .process(exchange -> {           
            MessageFlowProcessingException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, MessageFlowProcessingException.class);
            getLogger().error("Full exception trace", theException);
            getLogger().warn("Message flow exception - summary: {}", theException); 
            
            Long messageFlowId = getMessageFlowId(theException, exchange);
            
            if (!theException.isRetryable() && messageFlowId != null) {
                messageFlowService.recordMessageFlowError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true);

        
        // Handled Queue publishing exceptions.  For now retry everything. 
        onException(QueuePublishingException.class)
        .process(exchange -> {           
            QueuePublishingException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, QueuePublishingException.class);
            getLogger().error("Full exception trace", theException);
            getLogger().warn("Queue publishing exception - summary: {}", theException); 
            
            Long eventId = getEventId(theException, exchange);
            
            // If there was an event id we can mark the event for retry.
            if (eventId != null) {
                outboxService.markEventForRetry(eventId);
            }
  
            exchange.setRollbackOnly(true);
        })
        .handled(true);

        
        // Handled exceptions forwarding the message from a component.  For now retry everything. 
        onException(MessageForwardingException.class)
        .process(exchange -> {           
            MessageForwardingException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, MessageForwardingException.class);

            getLogger().error("Full exception trace", theException);
            getLogger().warn("Message forwarding exception - summary: {}", theException); 
            
            Long eventId = getEventId(theException, exchange);
            
            // If there was an event id we can mark the event for retry.
            if (eventId != null) {
                outboxService.markEventForRetry(eventId);
            }
  
            exchange.setRollbackOnly(true);
        })
        .handled(true);
        
        
        // Handled other types of exceptions.  If there is an id we can record an error.  No id means we need to retry.
        onException(Exception.class)
        .process(exchange -> {           
            Exception theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            getLogger().error("Full exception trace", theException);
            getLogger().warn("Unknown exception - summary: {}", theException); 
            
            // As this is just Exception and not a subclass of integration exception there is no id in the exception so see if there is a header set.
            Long messageFlowId = exchange.getIn().getHeader(IdentifierType.MESSAGE_FLOW_ID.name(), Long.class);
            
            if (messageFlowId != null) {
                messageFlowService.recordMessageFlowError(getIdentifier(), messageFlowId, new MessageFlowProcessingException("Unknown exception caught", messageFlowId, theException));
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true);       
    }

    
    protected void configureOutboxRoutes() throws ComponentConfigurationException, RouteConfigurationException {
       
        // A route to place the ingress message flow onto the egress queue
        from("direct:toEgressQueue-" + getIdentifier())
            .routeId("toEgressQueue-" + getIdentifier())
            .routeGroup(getComponentPath())  
            .transacted()
                .process(egressQueueProducerProcessor);
        
        // Event processor routes.
        from("timer://eventProcessorTimer-" + getIdentifier() + "?fixedRate=true&period=100&delay=2000")
        .routeId("eventProcessorTimer-" + getIdentifier())
        .process(exchange -> {
            Lock lock = null;
            
            try {
                IgniteCache<String, Integer> cache = ignite.getOrCreateCache("eventCache3");
                lock = cache.lock(getComponentPath());
    
                lock.lock(); // Lock acquired
    
                List<OutboxEventDto> events = outboxService.getEventsForComponent(getIdentifier(), 400);

                for (OutboxEventDto event : events) {
                    Exchange subExchange = exchange.copy();

                    subExchange.getIn().setHeader(IdentifierType.MESSAGE_FLOW_ID.name(), event.getMessageFlowId());
                    subExchange.getIn().setHeader(IdentifierType.OUTBOX_EVENT_ID.name(), event.getId());
                    subExchange.getIn().setBody(event.getId());
                    
                    String uri;
                    String route = eventRoutingMap.get(event.getType());
                    
                    if (route != null) {
                        uri = route + "-" + event.getComponentId();
                    } else {                        
                        continue; // skip unknown types
                    }
                    
                    exchange.getContext().createProducerTemplate().send("direct:" + uri, subExchange);
                }
            } finally {
                lock.unlock(); // Release lock after all events processed
            }
        });        
    }

    
    protected void configureStateChangeRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        // Timer to check the state of a component and take the appropriate action eg. stop, start or do nothing.
        from("timer://stateTimer-" + getIdentifier() + "?fixedRate=true&period=100&delay=2000")
        .routeId("stateTimer-" + getIdentifier())
        .process(exchange -> {
            ComponentDto component = componentConfigurationService.getComponent(identifier);
                
            // Process inbound state change
            if (component.getInboundState() != inboundState) {
                if (component.getInboundState() == IntegrationComponentStateEnum.RUNNING) {
                    camelContext.getRouteController().startRoute("ingress-" + getIdentifier());
                } else {
                    camelContext.getRouteController().stopRoute("ingress-" + getIdentifier());
                }
                
                inboundState = component.getInboundState();
            }
            
            // Process outbound state change
            if (component.getOutboundState() != outboundState) {
                if (component.getOutboundState() == IntegrationComponentStateEnum.RUNNING) {
                    camelContext.getRouteController().startRoute("egressForwarding-" + getIdentifier());
                } else {
                    camelContext.getRouteController().stopRoute("egressForwarding-" + getIdentifier());
                }
                
                outboundState = component.getOutboundState();
            }
        });        
    }

    
    /**
     * Gets the message flow id from either the exception or the exchange.
     * 
     * @param theException
     * @param exchange
     * @return
     */
    protected Long getMessageFlowId(IntegrationException theException, Exchange exchange) {
        Long messageFlowId = null;
        
        if (theException.hasIdentifier(IdentifierType.MESSAGE_FLOW_ID)) {
            messageFlowId = (Long)theException.getIdentifierValue(IdentifierType.MESSAGE_FLOW_ID);
        } else {
            messageFlowId = exchange.getIn().getHeader(IdentifierType.MESSAGE_FLOW_ID.name(), Long.class);
        }  
        
        return messageFlowId;
    }
    
    
    /**
     * Gets the event d from either the exception or the exchange.
     * 
     * @param theException
     * @param exchange
     * @return
     */
    protected Long getEventId(IntegrationException theException, Exchange exchange) {
        Long messageFlowId = null;
        
        if (theException.hasIdentifier(IdentifierType.OUTBOX_EVENT_ID)) {
            messageFlowId = (Long)theException.getIdentifierValue(IdentifierType.OUTBOX_EVENT_ID);
        } else {
            messageFlowId = exchange.getIn().getHeader(IdentifierType.OUTBOX_EVENT_ID.name(), Long.class);
        }  
        
        return messageFlowId;
    }

    
    @Override
    public void configure() throws Exception {
        configureComponentLevelExceptionHandlers();
        configureGlobalExceptionHandlers(this);
        
        configureIngressRoutes();
        configureEgressQueueConsumerRoutes();
        configureEgressForwardingRoutes();
        configureOutboxRoutes();

        configureStateChangeRoutes(); 
    }

    
    /**
     * Configures the ingress routes for a component.
     * 
     * @param routeBuilder
     * @throws RouteConfigurationException 
     * @throws ComponentConfigurationException 
     */
    protected abstract void configureIngressRoutes() throws ComponentConfigurationException, RouteConfigurationException;

    
    /**
     * Configures the egress queue consumer routes.
     * 
     * @throws ComponentConfigurationException
     * @throws RouteConfigurationException
     */
    protected abstract void configureEgressQueueConsumerRoutes() throws ComponentConfigurationException, RouteConfigurationException;

    
    /**
     * Configures the egress route for a component.
     * 
     * @param routeBuilder
     * @throws RouteConfigurationException 
     * @throws ComponentConfigurationException 
     */
    protected void configureEgressForwardingRoutes() throws ComponentConfigurationException, RouteConfigurationException  {
       
        // Component egress.  The exit point of a message in a component.
        from("direct:egressForwarding-" + getIdentifier())
            .routeId("egressForwarding-" + getIdentifier())
            .routeGroup(getComponentPath())
            .autoStartup(outboundState == IntegrationComponentStateEnum.RUNNING)
            .transacted()
                .process(exchange -> {                 
                    Long eventId = null;
                    Long messageFlowId = null;
                    
                    // Delete the event.
                    eventId = (long)exchange.getMessage().getHeader(IdentifierType.OUTBOX_EVENT_ID.name());
                    outboxService.deleteEvent(eventId);
                
                    messageFlowId = (Long)exchange.getMessage().getHeader(IdentifierType.MESSAGE_FLOW_ID.name());
                    MessageFlowDto messageFlowDto = messageFlowService.retrieveMessageFlow(messageFlowId);
                    
                    // Change the status of the message flow from pending forwarding to forwarded
                    messageFlowService.updateAction(messageFlowId, MessageFlowActionType.FORWARDED);
                       
                    forwardMessage(exchange, messageFlowDto, eventId);
            });
    }
    
    
    /**
     * The actual forwarding of the message.
     * 
     * @param exchange
     * @param messageFlowDto
     * @param eventId
     * @throws MessageForwardingException
     * @throws MessageFlowProcessingException 
     * @throws ComponentConfigurationException 
     */
    protected abstract void forwardMessage(Exchange exchange, MessageFlowDto messageFlowDto, long eventId) throws MessageForwardingException, ComponentConfigurationException, MessageFlowProcessingException;

    
    @Override
    public BaseRoute getRoute() {
        return route;
    }

    
    @Override
    public void setRoute(BaseRoute route) {
        this.route = route;
    }

    
    @Override
    public Map<String, String> getConfiguration() {
        return componentProperties;
    }
    
    
    @Override
    public void setConfiguration(Map<String, String> componentProperties) {
        this.componentProperties = componentProperties; 
    }

    
    @Override
    public IntegrationComponentStateEnum getInboundState() {
        return inboundState;
    }


    @Override
    public void setInboundState(IntegrationComponentStateEnum inboundState) {
        this.inboundState = inboundState;
    }

    
    @Override
    public IntegrationComponentStateEnum getOutboundState() {
        return outboundState;
    }

    
    @Override
    public void setOutboundState(IntegrationComponentStateEnum outboundState) {
        this.outboundState = outboundState;
    }

    
    @Override
    public IntegrationComponentCategoryEnum getCategory() throws ComponentConfigurationException {
        return getType().getCategory();
    }

    
    /**
     * Returns the component name.
     * @throws ComponentConfigurationException 
     */
    @Override
    public String getName() throws ComponentConfigurationException {
        IntegrationComponent annotation = getRequiredAnnotation(IntegrationComponent.class);

        return annotation.name();
    }

    
    /**
     * Returns the component type.
     * @throws ComponentConfigurationException 
     */
    @Override
    public IntegrationComponentTypeEnum getType() throws ComponentConfigurationException {
        ComponentType annotation = getRequiredAnnotation(ComponentType.class);

        return annotation.type();
    }

    
    /**
     * Returns the content type handled by this component.
     * 
     * @return
     * @throws ComponentConfigurationException 
     */
    public ContentTypeEnum getContentType() throws ComponentConfigurationException {
        AllowedContentType annotation = getRequiredAnnotation(AllowedContentType.class);
        return annotation.value();
    }

    
    /**
     * A helper method to get a required annotation.
     * 
     * @param <T>
     * @param annotationClass
     * @return
     */
    protected <T extends Annotation> T getRequiredAnnotation(Class<T> annotationClass) throws ComponentConfigurationException {
        T annotation = this.getClass().getAnnotation(annotationClass);

        if (annotation == null) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(IdentifierType.COMPONENT_ID, getIdentifier()));
            throw new ComponentConfigurationException("Missing required annotation @" + annotationClass.getSimpleName() + " on class " + this.getClass().getName() + " or its hierarchy.", getIdentifier());
        }

        return annotation;
    }
    
    
    /**
     * A helper method to get an annotation.
     * 
     * @param <T>
     * @param annotationClass
     * @return
     */
    protected <T extends Annotation> T getAnnotation(Class<T> annotationClass) throws ComponentConfigurationException {
        T annotation = this.getClass().getAnnotation(annotationClass);

        return annotation;
    }
    
    
    /**
     * 
     */
    protected void configureComponentLevelExceptionHandlers() {
        
    }

    
    /**
     * Gets a message flow DTO from the message flow id in the exchange body.
     * 
     * @param exchange
     * @return
     * @throws MessageFlowProcessingException
     * @throws MessageFlowNotFoundException
     */
    protected MessageFlowDto getMessageFlowDtoFromExchangeBody(Exchange exchange) throws MessageFlowProcessingException, MessageFlowNotFoundException {
        Long parentMessageFlowId = exchange.getMessage().getBody(Long.class);
        exchange.getMessage().setHeader(IdentifierType.MESSAGE_FLOW_ID.name(), parentMessageFlowId);
        
        return messageFlowService.retrieveMessageFlow(parentMessageFlowId);
    }
    
    
    /**
     * The default routing.  Subclasses can override if required.
     */
    protected void configureEventRouting() {
        eventRoutingMap.put(OutboxEventType.INGRESS_COMPLETE, "toEgressQueue");
        eventRoutingMap.put(OutboxEventType.PENDING_FORWARDING, "egressForwarding");
    }
}
