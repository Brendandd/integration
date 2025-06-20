package integration.core.runtime.messaging.component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteSet;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import integration.core.domain.IdentifierType;
import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.configuration.IntegrationComponentCategoryEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.dto.ComponentDto;
import integration.core.dto.InboxEventDto;
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
import integration.core.runtime.messaging.exception.retryable.InboxEventSchedulerException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.exception.retryable.OutboxEventSchedulerException;
import integration.core.runtime.messaging.service.InboxService;
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
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMessagingComponent extends RouteBuilder implements MessagingComponent {   
          
    protected long identifier;
    protected BaseRoute route;
    
    protected IntegrationComponentStateEnum inboundState;
    protected IntegrationComponentStateEnum outboundState;
    
    @Autowired
    protected ComponentService componentConfigurationService;
            
    @Autowired
    protected CamelContext camelContext;
    
    @Autowired
    protected ApplicationContext springContext;
    
    @Autowired
    protected MessageFlowService messageFlowService;
    
    @Autowired
    protected OutboxService outboxService;
    
    @Autowired
    protected InboxService inboxService;
    
    protected Map<String,String>componentProperties;

    @Autowired
    protected Ignite ignite;
    
    @Autowired
    protected Environment env;
    
    @Autowired
    protected ProducerTemplate producerTemplate;
    
    @Autowired
    protected MessageFlowPropertyService messageFlowPropertyService;
       
    protected final Set<Class<? extends Annotation>> requiredAnnotations = new LinkedHashSet<>();

    public abstract Logger getLogger();

    
    @PostConstruct
    public void BaseMessagingComponentInit() {
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
                
        // Handle exceptions caught by the outbox event scheduler. 
        onException(OutboxEventSchedulerException.class)
        .maximumRedeliveries(0)
        .process(exchange -> {           
            OutboxEventSchedulerException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, OutboxEventSchedulerException.class);
            getLogger().error("Full exception trace", theException);
            getLogger().warn("Outbox Event Scheduler Exception - summary: {}", theException); 
            
            Long eventId = getEventId(theException, exchange);
            
            // If there was an event id we can mark the event for retry.
            if (theException.isRetryable() && eventId != null) {
                outboxService.markEventForRetry(eventId, theException);
                exchange.setRollbackOnly(true);
            }
            
            //TODO need to set event as failed if not retryable.
        })
        .handled(exchange -> {
            OutboxEventSchedulerException ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, OutboxEventSchedulerException.class);
            return !ex.isRetryable();
        });

        
        // Handle exceptions caught by the inbox event scheduler. 
        onException(InboxEventSchedulerException.class)
        .maximumRedeliveries(0)
        .process(exchange -> {           
            InboxEventSchedulerException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, InboxEventSchedulerException.class);
            getLogger().error("Full exception trace", theException);
            getLogger().warn("Outbox Event Scheduler Exception - summary: {}", theException); 
            
            Long eventId = getEventId(theException, exchange);
            
            // If there was an event id we can mark the event for retry.
            if (theException.isRetryable() && eventId != null) {
                inboxService.markEventForRetry(eventId, theException);
                exchange.setRollbackOnly(true);
            }
            
            //TODO need to set event as failed if not retryable.
        })
        .handled(exchange -> {
            InboxEventSchedulerException ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, InboxEventSchedulerException.class);
            return !ex.isRetryable();
        });

        
        // Handled other types of exceptions.  If there is an id we can record an error.  No id means we need to retry.
        onException(Exception.class)
        
        .process(exchange -> {           
            Exception theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            
            theException.printStackTrace();
            
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
        .handled(exchange -> {
            Long messageFlowId = exchange.getIn().getHeader(IdentifierType.MESSAGE_FLOW_ID.name(), Long.class);
            return messageFlowId != null;
        });       
    }

    
    protected void configureOutboxRoutes() throws ComponentConfigurationException, RouteConfigurationException {       
        ExecutorService executor = Executors.newFixedThreadPool(20);
        IgniteCache<String, Integer> cache = ignite.getOrCreateCache("eventCache3");
        IgniteSet<Long> igniteOutboxEventInProgressSet = ignite.set("processingOutboxEvents" + getIdentifier(), new CollectionConfiguration());

       
        // Event processor routes.
        from("timer://outboxEventProcessorTimer-" + getIdentifier() + "?period=300&delay=2000")
        .routeId("outboxEventProcessorTimer-" + getIdentifier())
        .process(exchange -> {
            Lock eventSelectionLock = cache.lock("outbox-event-selection-lock-" + getIdentifier());

            List<OutboxEventDto> events = null;
            
            eventSelectionLock.lock();
            try {
                List<Long> idsToExclude = new ArrayList<>();
                if (igniteOutboxEventInProgressSet != null && !igniteOutboxEventInProgressSet.isEmpty()) {
                    idsToExclude.addAll(igniteOutboxEventInProgressSet);
                }

                // Select the records to process while holding the lock.
                events = outboxService.getEventsForComponent(getIdentifier(), 50, idsToExclude);
                

                for (OutboxEventDto event : events) {
                    igniteOutboxEventInProgressSet.add(event.getId());
                }   
            } finally {
                eventSelectionLock.unlock();
            }

            for (OutboxEventDto event : events) {
                executor.submit(() -> {
                    
                    try {
                        Map<String, Object> headers = new HashMap<>();
                        headers.put(IdentifierType.MESSAGE_FLOW_ID.name(), event.getMessageFlowId());
                        headers.put(IdentifierType.OUTBOX_EVENT_ID.name(), event.getId());

                        producerTemplate.sendBodyAndHeaders("direct:processOutboxEvent-" + event.getComponentId(), event.getMessageFlowId(), headers);   
                    } finally {
                        igniteOutboxEventInProgressSet.remove(event.getId());
                    }
                });                   
            }
        });    

        
        IgniteSet<Long> igniteInboxEventInProgressSet = ignite.set("processingInboxEvents" + getIdentifier(), new CollectionConfiguration());
        
        // Event processor routes.
        from("timer://inboxEventProcessorTimer-" + getIdentifier() + "?period=300&delay=2000")
        .routeId("inboxEventProcessorTimer-" + getIdentifier())
        .process(exchange -> {
            Lock eventSelectionLock = cache.lock("inbox-event-selection-lock-" + getIdentifier());

            List<InboxEventDto> events = null;
            
            eventSelectionLock.lock();
            try {
                List<Long> idsToExclude = new ArrayList<>();
                if (igniteInboxEventInProgressSet != null && !igniteInboxEventInProgressSet.isEmpty()) {
                    idsToExclude.addAll(igniteInboxEventInProgressSet);
                }

                // Select the records to process while holding the lock.
                events = inboxService.getEventsForComponent(getIdentifier(), 50, idsToExclude);
                
                for (InboxEventDto event : events) {
                    igniteInboxEventInProgressSet.add(event.getId());
                }   
            } finally {
                eventSelectionLock.unlock();
            }

            for (InboxEventDto event : events) {
                executor.submit(() -> {
                    
                    try {
                        Map<String, Object> headers = new HashMap<>();
                        headers.put(IdentifierType.MESSAGE_FLOW_ID.name(), event.getMessageFlowId());
                        headers.put(IdentifierType.OUTBOX_EVENT_ID.name(), event.getId());

                        producerTemplate.sendBodyAndHeaders("direct:processInboxEvent-" + event.getComponentId(), event.getMessageFlowId(), headers);   
                    } finally {
                        igniteInboxEventInProgressSet.remove(event.getId());
                    }
                });                   
            }
        });  

        
        from("direct:processInboxEvent-" + getIdentifier())
        .routeId("processInboxEvent-" + getIdentifier())
        .routeGroup(getComponentPath())
        .transacted("jpaTransactionPolicy")
            .process(getInboxEventProcessor()); 

        
        from("direct:processOutboxEvent-" + getIdentifier())
        .routeId("processOutboxEvent-" + getIdentifier())
        .routeGroup(getComponentPath())
        .transacted("jpaTransactionPolicy")
            .process(getOutboxEventProcessor());  
    }

    
    protected void configureStateChangeRoutes() throws ComponentConfigurationException, RouteConfigurationException {
        // Timer to check the state of a component and take the appropriate action eg. stop, start or do nothing.
        from("timer://stateTimer-" + getIdentifier() + "?period=30000&delay=2000")
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

    
    /**
     *
     */
    @Override
    public void configure() throws Exception {
        configureComponentLevelExceptionHandlers();
        configureGlobalExceptionHandlers(this);
        
        configureIngressRoutes();
        configureOutboxRoutes();

        configureStateChangeRoutes(); 
    }

    
    /**
     * Configures the ingress routes for a component.
     *
     * @throws RouteConfigurationException 
     * @throws ComponentConfigurationException 
     */
    protected abstract void configureIngressRoutes() throws ComponentConfigurationException, RouteConfigurationException;

           
    
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
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) throws ComponentConfigurationException {
        return this.getClass().getAnnotation(annotationClass);
    }
    
    
    /**
     * Configures any exception handlers in the sub classes.
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
        
        return messageFlowService.retrieveMessageFlow(parentMessageFlowId, false);
    }

    
    @Override
    public String getOwner() {
        return env.getProperty("owner");  
    } 

    
    /**
     * Get this components inbox event processor.
     * 
     * @return
     */
    public abstract InboxEventProcessor getInboxEventProcessor();

    
    /**
     * Get this components outbox event processor.
     * 
     * @return
     */
    public abstract OutboxEventProcessor getOutboxEventProcessor();
}
