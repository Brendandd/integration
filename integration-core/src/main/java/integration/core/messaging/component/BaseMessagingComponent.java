package integration.core.messaging.component;

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
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import integration.core.domain.configuration.ComponentCategoryEnum;
import integration.core.domain.configuration.ComponentStateEnum;
import integration.core.domain.configuration.ComponentTypeEnum;
import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.ComponentDto;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.MessageFlowEventDto;
import integration.core.exception.ConfigurationException;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.messaging.BaseRoute;
import integration.core.messaging.MessageFlowException;
import integration.core.messaging.OutboxProcessingException;
import integration.core.messaging.component.annotation.ComponentType;
import integration.core.messaging.component.annotation.IntegrationComponent;
import integration.core.messaging.component.type.handler.filter.FilterException;
import integration.core.messaging.component.type.handler.splitter.SplitterException;
import integration.core.messaging.component.type.handler.transformation.TransformationException;
import integration.core.service.ConfigurationService;
import integration.core.service.MessageFlowPropertyService;
import integration.core.service.MessagingFlowService;

/**
 * Base class for all Apache Camel messaging component routes.
 * 
 * All messaging components are implemented as multiple Apache Camel routes. All components are separated into inbound message handling and outbound message handling with a JMS queue allowing communication between
 * the two.  
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
    public static final String MESSAGE_FLOW_ID = "messageFlowId";
    public static final String EVENT_ID = "eventId";
    
    protected long identifier;
    protected BaseRoute route;
    protected String owner;
    
    protected ComponentStateEnum inboundState;
    protected ComponentStateEnum outboundState;
    
    @Autowired
    protected ConfigurationService configurationService;
    
    @Autowired
    protected CamelContext camelContext;
    
    @Autowired
    protected ApplicationContext springContext;
    
    @Autowired
    protected MessagingFlowService messagingFlowService;
    
    protected Map<String,String>componentProperties;

    @Autowired
    protected Ignite ignite;
    
    @Autowired
    private Environment env;
    
    @Autowired
    protected ProducerTemplate producerTemplate;
    
    @Autowired
    protected MessageFlowPropertyService messageFlowPropertyService;
    
    protected Set<Class<? extends Annotation>> requiredAnnotations = new LinkedHashSet<>();

    public abstract Logger getLogger();

    /**
     * Makes sure each component has the mandatory annotations for its type.
     * @throws ConfigurationException 
     */
    public void validateAnnotations() throws ConfigurationException {
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
                List<ExceptionIdentifier>identifiers = new ArrayList<>();
                identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, getIdentifier()));
                throw new ConfigurationException("Missing required annotation @" + required.getSimpleName() + " on class " + this.getClass().getName() + " or its hierarchy.", identifiers, false);
            }
        }
        
        getLogger().info("All required annotations found on component: {}", getIdentifier());
    } 

    
    /**
     * 
     */
    protected abstract void configureRequiredAnnotations();


    /**
     * Where to forward the message too.  This is a Camel uri. 
     * 
     * @return
     * @throws ConfigurationException 
     */
    public abstract String getMessageForwardingUriString(Exchange exchange) throws ConfigurationException;

    
    /**
     * The full component path.  owner-route-path
     */
    @Override
    public String getComponentPath() throws ConfigurationException {
        return getOwner() + "-" + route.getName() + "-" + getName();
    }

    
    @Override
    public long getIdentifier() {
        return identifier;
    }

    
    @Override
    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    
    @Override
    public void configure() throws Exception {
        
        // Handled errors processing events from the outbox.
        onException(OutboxProcessingException.class)
        .process(exchange -> {
            Long eventId = null;
            boolean isRetryable = true;
            
            OutboxProcessingException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, OutboxProcessingException.class);
            getLogger().error("Event processing exception - " + theException.toString());
            
            
            if (theException.hasIdentifier(ExceptionIdentifierType.EVENT_ID)) {
                eventId = (Long)theException.getIdentifierValue(ExceptionIdentifierType.EVENT_ID);
                isRetryable = theException.isRetryable();
            } else {
                eventId = exchange.getIn().getHeader(BaseMessagingComponent.EVENT_ID, Long.class);
            }
            
            if (isRetryable && eventId != null) {
                messagingFlowService.setEventFailed(eventId);
            }

            exchange.setRollbackOnly(true);
        })
        .handled(true); // handled is true but the outbox process will keep retrying.

        
        // Handle transformation errors.
        onException(TransformationException.class)
        .process(exchange -> {
            Long messageFlowId = null;
            boolean isRetryable = true;
            
            TransformationException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, TransformationException.class);
            getLogger().error("Transformation exception - " + theException.toString());
            
            isRetryable = theException.isRetryable();
            
            if (theException.hasIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID)) {
                messageFlowId = (Long)theException.getIdentifierValue(ExceptionIdentifierType.MESSAGE_FLOW_ID);
            } else {
                messageFlowId = exchange.getIn().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID, Long.class);
            }
            
            if (!isRetryable && messageFlowId != null) {
                messagingFlowService.recordTransformationError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true); 

        
        // Handle splitter errors
        onException(SplitterException.class)
        .process(exchange -> {
            Long messageFlowId = null;
            boolean isRetryable = true;
            
            SplitterException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, SplitterException.class);
            getLogger().error("Splitter exception - " + theException.toString());
            
            isRetryable = theException.isRetryable();
            
            if (theException.hasIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID)) {
                messageFlowId = (Long)theException.getIdentifierValue(ExceptionIdentifierType.MESSAGE_FLOW_ID);
            } else {
                messageFlowId = exchange.getIn().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID, Long.class);
            }
            
            if (!isRetryable && messageFlowId != null) {
                messagingFlowService.recordSplitterError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true); 

        
        // Handle filter errors
        onException(FilterException.class)
        .process(exchange -> {
            Long messageFlowId = null;
            boolean isRetryable = true;
            
            FilterException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, FilterException.class);
            getLogger().error("Filter exception - " + theException.toString());
            
            isRetryable = theException.isRetryable();
            
            if (theException.hasIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID)) {
                messageFlowId = (Long)theException.getIdentifierValue(ExceptionIdentifierType.MESSAGE_FLOW_ID);
            } else {
                messageFlowId = exchange.getIn().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID, Long.class);
            }
            
            if (!isRetryable && messageFlowId != null) {
                messagingFlowService.recordFilterError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true); 

        
        // Handled errors processing events from the outbox.
        onException(MessageFlowException.class)
        .process(exchange -> {
            Long messageFlowId = null;
            boolean isRetryable = true;
            
            MessageFlowException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, MessageFlowException.class);
            getLogger().error("Message flow exception - " + theException.toString());
            
            isRetryable = theException.isRetryable();
            
            if (theException.hasIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID)) {
                messageFlowId = (Long)theException.getIdentifierValue(ExceptionIdentifierType.MESSAGE_FLOW_ID);
            } else {
                messageFlowId = exchange.getIn().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID, Long.class);
            }
            
            if (!isRetryable && messageFlowId != null) {
                // call the service to fail the flow.
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true);

        
        // A route to add the message flow step id to the inbound message handling complete queue.
        from("direct:processComponentInboundMessageHandlingCompleteEvent-" + getIdentifier())
            .routeId("processComponentInboundMessageHandlingCompleteEvent-" + getIdentifier())
            .routeGroup(getComponentPath())  
            .transacted()
                .process(new Processor() {
    
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Long eventId = null;
                        Long messageFlowId = null;
                        
                        try {
                            // Delete the event.
                            eventId = exchange.getMessage().getBody(Long.class);
                            messagingFlowService.deleteEvent(eventId);
                            
                            // Get the message flow step id.
                            messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID);
    
                            // Add the message to the queue
                            producerTemplate.sendBody("jms:queue:inboundMessageHandlingComplete-" + getIdentifier(), messageFlowId);
                        } catch (Exception e) {
                            List<ExceptionIdentifier>identifiers = new ArrayList<>();
                            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, getIdentifier()));
                            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.EVENT_ID, eventId));
                            throw new OutboxProcessingException("Error adding the message flow id to the internal processing queue", identifiers, e);
                        }
                    }
                });

        
        // A route which reads from the components internal message handling complete queue.  This is the entry point for a components outbound message handling.
        from("jms:queue:inboundMessageHandlingComplete-" + getIdentifier() + "?acknowledgementModeName=CLIENT_ACKNOWLEDGE&concurrentConsumers=5")
            .routeId("outboundEntryPoint-" + getIdentifier())
            .routeGroup(getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .transacted()            
                // All components must provide an outboudMessageHandling route.
                .to("direct:outboundMessageHandling-" + getIdentifier());

        
        
        // Event processor routes.
        from("timer://eventProcessorTimer-" + getIdentifier() + "?fixedRate=true&period=100&delay=2000")
        .routeId("eventProcessorTimer-" + getIdentifier())
        .process(exchange -> {
            IgniteCache<String, Integer> cache = ignite.getOrCreateCache("eventCache3");
            Lock lock = cache.lock(getComponentPath());

            lock.lock(); // Lock acquired

            try {
                List<MessageFlowEventDto> events = messagingFlowService.getEventsForComponent(getIdentifier(), 400);

                for (MessageFlowEventDto event : events) {
                    Exchange subExchange = exchange.copy();

                    subExchange.getIn().setHeader(BaseMessagingComponent.MESSAGE_FLOW_ID, event.getMessageFlowId());
                    subExchange.getIn().setHeader("eventId", event.getId());
                    subExchange.getIn().setBody(event.getId());
                    
                    String uri;
                    if (event.getType() == MessageFlowEventType.COMPONENT_INBOUND_MESSAGE_HANDLING_COMPLETE) {
                        uri = "processComponentInboundMessageHandlingCompleteEvent" + "-" + event.getComponentId();
                    } else if (event.getType() == MessageFlowEventType.COMPONENT_OUTBOUND_MESSAGE_HANDLING_COMPLETE) {
                        uri = "processComponentOutboundMessageHandlingCompleteEvent" + "-" + event.getComponentId();
                    } else {
                        continue; // skip unknown types
                    }

                    exchange.getContext().createProducerTemplate().send("direct:" + uri, subExchange);
                }

            } finally {
                lock.unlock(); // Release lock after all events processed
            }
        });
    
    
        // Timer to check the state of a component and take the appropriate action eg. stop, start or do nothing.
        from("timer://stateTimer-" + getIdentifier() + "?fixedRate=true&period=100&delay=2000")
        .routeId("stateTimer-" + getIdentifier())
        .process(exchange -> {
            ComponentDto component = configurationService.getComponent(identifier);
                
            // Process inbound state change
            if (component.getInboundState() != inboundState) {
                if (component.getInboundState() == ComponentStateEnum.RUNNING) {
                    camelContext.getRouteController().startRoute("inboundEntryPoint-" + getIdentifier());
                } else {
                    camelContext.getRouteController().stopRoute("inboundEntryPoint-" + getIdentifier());
                }
                
                inboundState = component.getInboundState();
            }
            
            // Process outbound state change
            if (component.getOutboundState() != outboundState) {
                if (component.getOutboundState() == ComponentStateEnum.RUNNING) {
                    camelContext.getRouteController().startRoute("outboundExitPoint-" + getIdentifier());
                } else {
                    camelContext.getRouteController().stopRoute("outboundExitPoint-" + getIdentifier());
                }
                
                outboundState = component.getOutboundState();
            }
        });

        
        // A route to process outbound message handling complete events.  This is the final stage of a components processing.
        from("direct:processComponentOutboundMessageHandlingCompleteEvent-" + getIdentifier())
            .routeId("outboundExitPoint-" + getIdentifier())
            .routeGroup(getComponentPath())
            .autoStartup(outboundState == ComponentStateEnum.RUNNING)
            .transacted()
            .process(new Processor() {

                @Override
                public void process(Exchange exchange) throws Exception { 
                    Long eventId = null;
                    Long messageFlowId = null;
                    
                    try {
                        // Delete the event.
                        eventId = (long)exchange.getMessage().getHeader(BaseMessagingComponent.EVENT_ID);
                        messagingFlowService.deleteEvent(eventId);
                    
                        messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID);
                        MessageFlowDto messageFlowDto = messagingFlowService.retrieveMessageFlow(messageFlowId);
                        
                        // Change the status of the message flow from pending forwarding to forwarded
                        messagingFlowService.updateAction(messageFlowId, MessageFlowActionType.FORWARDED);
                        
                        
                        // Do any pre forwarding processing.  Subclasses can provide their own.  The default is no processing.
                        preForwardingProcessing(exchange);
                        
                        producerTemplate.sendBodyAndHeaders(getMessageForwardingUriString(exchange), getBodyContent(messageFlowDto), getHeaders(exchange, messageFlowDto.getId()));
                    } catch(Exception e) {
                        List<ExceptionIdentifier>identifiers = new ArrayList<>();
                        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, getIdentifier()));
                        identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.EVENT_ID, eventId));
                        throw new OutboxProcessingException("Error forwarding message from outbox", identifiers, e);
                    }
                }
            });
     }

    
    /**
     * Custom processing required by a component before forwarding the message eg. set headers.
     * 
     * @param exchange
     */
    protected void preForwardingProcessing(Exchange exchange) throws MessageFlowException, ConfigurationException {
        // The default is nothing.
    }

    
    protected Map<String, Object>getHeaders(Exchange exchange, long messageFlowId) throws MessageFlowException, ConfigurationException {
        return new HashMap<String, Object>();
    }

    
    /**
     * Get the body content that needs to be forwarded to the next component/send externally.
     * 
     * @param messageFlowDto
     * @return
     */
    protected abstract Object getBodyContent(MessageFlowDto messageFlowDto);
    
    
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
    public ComponentStateEnum getInboundState() {
        return inboundState;
    }


    @Override
    public void setInboundState(ComponentStateEnum inboundState) {
        this.inboundState = inboundState;
    }

    
    @Override
    public ComponentStateEnum getOutboundState() {
        return outboundState;
    }

    
    @Override
    public void setOutboundState(ComponentStateEnum outboundState) {
        this.outboundState = outboundState;
    }

    
    @Override
    public String getOwner() {
        return env.getProperty("owner");
    }

    
    @Override
    public ComponentCategoryEnum getCategory() throws ConfigurationException {
        return getType().getCategory();
    }

    
    /**
     * Returns the component name.
     */
    @Override
    public String getName() throws ConfigurationException {
        IntegrationComponent annotation = getRequiredAnnotation(IntegrationComponent.class);

        return annotation.name();
    }

    
    /**
     * Returns the component type.
     */
    @Override
    public ComponentTypeEnum getType() throws ConfigurationException {
        ComponentType annotation = getRequiredAnnotation(ComponentType.class);

        return annotation.type();
    }

    
    /**
     * Returns the content type handled by this component.
     * 
     * @return
     * @throws ConfigurationException 
     */
    public ContentTypeEnum getContentType() throws ConfigurationException {
        AllowedContentType annotation = getRequiredAnnotation(AllowedContentType.class);
        return annotation.value();
    }

    
    /**
     * A helper method to get a required annotation.
     * 
     * @param <T>
     * @param annotationClass
     * @return
     * @throws ConfigurationException If the annotation is not found.
     */
    protected <T extends Annotation> T getRequiredAnnotation(Class<T> annotationClass) throws ConfigurationException {
        T annotation = this.getClass().getAnnotation(annotationClass);

        if (annotation == null) {
            List<ExceptionIdentifier>identifiers = new ArrayList<>();
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, getIdentifier()));
            throw new ConfigurationException("@" + annotationClass.getSimpleName() + " annotation not found. It is mandatory for all components", identifiers, false);
        }

        return annotation;
    }

}
