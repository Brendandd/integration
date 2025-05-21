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
import org.apache.camel.CamelExecutionException;
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

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.domain.configuration.IntegrationComponentCategoryEnum;
import integration.core.domain.configuration.IntegrationComponentStateEnum;
import integration.core.domain.configuration.IntegrationComponentTypeEnum;
import integration.core.domain.messaging.MessageFlowActionType;
import integration.core.domain.messaging.MessageFlowEventType;
import integration.core.dto.ComponentDto;
import integration.core.dto.MessageFlowDto;
import integration.core.dto.MessageFlowEventDto;
import integration.core.exception.ExceptionIdentifier;
import integration.core.exception.ExceptionIdentifierType;
import integration.core.exception.IntegrationException;
import integration.core.runtime.messaging.BaseRoute;
import integration.core.runtime.messaging.component.annotation.ComponentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.core.runtime.messaging.component.type.handler.filter.FilterException;
import integration.core.runtime.messaging.component.type.handler.splitter.SplitterException;
import integration.core.runtime.messaging.component.type.handler.transformation.TransformationException;
import integration.core.runtime.messaging.exception.nonretryable.ComponentConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.ConfigurationException;
import integration.core.runtime.messaging.exception.nonretryable.RouteConfigurationException;
import integration.core.runtime.messaging.exception.retryable.MessageFlowProcessingException;
import integration.core.runtime.messaging.service.MessageFlowEventService;
import integration.core.runtime.messaging.service.MessageFlowPropertyService;
import integration.core.runtime.messaging.service.MessagingFlowService;
import integration.core.service.ComponentService;
import jakarta.jms.JMSException;

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
    
    protected IntegrationComponentStateEnum inboundState;
    protected IntegrationComponentStateEnum outboundState;
    
    @Autowired
    protected ComponentService componentConfigurationService;
    
    @Autowired
    protected CamelContext camelContext;
    
    @Autowired
    protected ApplicationContext springContext;
    
    @Autowired
    protected MessagingFlowService messagingFlowService;
    
    @Autowired
    protected MessageFlowEventService messageFlowEventService;
    
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

    
    /**
     * 
     */
    protected abstract void configureRequiredAnnotations();


    /**
     * Where to forward the message too.  This is a Camel uri. 
     * 
     * @return
     * @throws ComponentConfigurationException 
     * @throws RouteConfigurationException 
     */
    public abstract String getMessageForwardingUriString(Exchange exchange) throws ComponentConfigurationException, RouteConfigurationException;

    
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
     * Gets the message flow id from eithe the exception or the exchange.
     * 
     * @param theException
     * @param exchange
     * @return
     */
    public Long getMessageFlowId(IntegrationException theException, Exchange exchange) {
        Long messageFlowId = null;
        
        if (theException.hasIdentifier(ExceptionIdentifierType.MESSAGE_FLOW_ID)) {
            messageFlowId = (Long)theException.getIdentifierValue(ExceptionIdentifierType.MESSAGE_FLOW_ID);
        } else {
            messageFlowId = exchange.getIn().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID, Long.class);
        }  
        
        return messageFlowId;
    }
    
    
    @Override
    public void configure() throws Exception {

        // Handle transformation errors.
        onException(TransformationException.class)
        .process(exchange -> {           
            TransformationException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, TransformationException.class);
            getLogger().error("Transformation exception - " + theException.toString());
            
            Long messageFlowId = getMessageFlowId(theException, exchange);
                    
            if (!theException.isRetryable() && messageFlowId != null) {
                messagingFlowService.recordTransformationError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true); 

        
        // Handle splitter errors
        onException(SplitterException.class)
        .process(exchange -> {            
            SplitterException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, SplitterException.class);
            getLogger().error("Splitter exception - " + theException.toString());
            
            Long messageFlowId = getMessageFlowId(theException, exchange);
            
            if (!theException.isRetryable() && messageFlowId != null) {
                messagingFlowService.recordSplitterError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true); 

        
        // Handle filter errors
        onException(FilterException.class)
        .process(exchange -> {           
            FilterException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, FilterException.class);
            getLogger().error("Filter exception - " + theException.toString());
            
            Long messageFlowId = getMessageFlowId(theException, exchange);
            
            if (!theException.isRetryable() && messageFlowId != null) {
                messagingFlowService.recordFilterError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true); 

        
        // Handled other message flow exceptions
        onException(MessageFlowProcessingException.class)
        .process(exchange -> {           
            MessageFlowProcessingException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, MessageFlowProcessingException.class);
            getLogger().error("Message flow exception - " + theException.toString());
            
            Long messageFlowId = getMessageFlowId(theException, exchange);
            
            if (!theException.isRetryable() && messageFlowId != null) {
                messagingFlowService.recordMessageFlowError(getIdentifier(), messageFlowId, theException);
            } else {
                exchange.setRollbackOnly(true); 
            }
        })
        .handled(true);

        
        // Handled JMS exceptions.  These will come from outbox processing so we just sent the event as failed so it will pick it up again later.  No need to fail the message flow.
        onException(JMSException.class)
        .process(exchange -> {           
            JMSException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, JMSException.class);
            getLogger().error("JSM exception - " + theException.toString());
            
            Long eventId = (Long)exchange.getMessage().getHeader(EVENT_ID);
            
            if (eventId != null) {
                messageFlowEventService.setEventFailed(eventId);
            }
  
            exchange.setRollbackOnly(true);
        })
        .handled(true);
        
        
        // Handled MLLP exceptions.  These will come from outbox processing so we just sent the event as failed so it will pick it up again later.  No need to fail the message flow.
        onException(CamelExecutionException.class)
        .process(exchange -> {           
            CamelExecutionException theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, CamelExecutionException.class);
            getLogger().error("Camel exception - " + theException.toString());
            
            Long eventId = (Long)exchange.getMessage().getHeader(EVENT_ID);

            if (eventId != null) {
                messageFlowEventService.setEventFailed(eventId);
            }
            
            exchange.setRollbackOnly(true);
        })
        .handled(true);

        
        // Handled other types of exceptions.  If there is an id we can record an error.  No id means we need to retry.
        onException(Exception.class)
        .process(exchange -> {           
            Exception theException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
            getLogger().error("Unknown exception - " + theException.toString());
            
            // As this is just Exception and not a subclass of integration exception there is no id in the exception so see if there is a header set.
            Long messageFlowId = exchange.getIn().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID, Long.class);
            
            if (messageFlowId != null) {
                messagingFlowService.recordMessageFlowError(getIdentifier(), messageFlowId, new MessageFlowProcessingException("Unknown exception caught", messageFlowId, theException));
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
                        
                        // Delete the event.
                        eventId = exchange.getMessage().getBody(Long.class);
                        exchange.getMessage().setHeader(EVENT_ID, eventId);
                        
                        messageFlowEventService.deleteEvent(eventId);
                        
                        // Get the message flow step id.
                        messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID);

                        // Add the message to the queue
                        producerTemplate.sendBody("jms:queue:inboundMessageHandlingComplete-" + getIdentifier(), messageFlowId);
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
            Lock lock = null;
            
            try {
                IgniteCache<String, Integer> cache = ignite.getOrCreateCache("eventCache3");
                lock = cache.lock(getComponentPath());
    
                lock.lock(); // Lock acquired
    
                List<MessageFlowEventDto> events = messageFlowEventService.getEventsForComponent(getIdentifier(), 400);

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
            ComponentDto component = componentConfigurationService.getComponent(identifier);
                
            // Process inbound state change
            if (component.getInboundState() != inboundState) {
                if (component.getInboundState() == IntegrationComponentStateEnum.RUNNING) {
                    camelContext.getRouteController().startRoute("inboundEntryPoint-" + getIdentifier());
                } else {
                    camelContext.getRouteController().stopRoute("inboundEntryPoint-" + getIdentifier());
                }
                
                inboundState = component.getInboundState();
            }
            
            // Process outbound state change
            if (component.getOutboundState() != outboundState) {
                if (component.getOutboundState() == IntegrationComponentStateEnum.RUNNING) {
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
            .autoStartup(outboundState == IntegrationComponentStateEnum.RUNNING)
            .transacted()
            .process(new Processor() {

                @Override
                public void process(Exchange exchange) throws Exception { 
                    Long eventId = null;
                    Long messageFlowId = null;
                    
                    // Delete the event.
                    eventId = (long)exchange.getMessage().getHeader(BaseMessagingComponent.EVENT_ID);
                    messageFlowEventService.deleteEvent(eventId);
                
                    messageFlowId = (Long)exchange.getMessage().getHeader(BaseMessagingComponent.MESSAGE_FLOW_ID);
                    MessageFlowDto messageFlowDto = messagingFlowService.retrieveMessageFlow(messageFlowId);
                    
                    // Change the status of the message flow from pending forwarding to forwarded
                    messagingFlowService.updateAction(messageFlowId, MessageFlowActionType.FORWARDED);
                    
                    // Do any pre forwarding processing.  Subclasses can provide their own.  The default is no processing.
                    preForwardingProcessing(exchange);
                   
                    producerTemplate.sendBodyAndHeaders(getMessageForwardingUriString(exchange), getBodyContent(messageFlowDto), getHeaders(exchange, messageFlowDto.getId()));
                }
            });
     }

    
    /**
     * Custom processing required by a component before forwarding the message eg. set headers.
     * 
     * @param exchange
     */
    protected void preForwardingProcessing(Exchange exchange) throws MessageFlowProcessingException, ConfigurationException {
        // The default is nothing.
    }

    
    protected Map<String, Object>getHeaders(Exchange exchange, long messageFlowId) throws MessageFlowProcessingException, ConfigurationException, ComponentConfigurationException {
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
            identifiers.add(new ExceptionIdentifier(ExceptionIdentifierType.COMPONENT_ID, getIdentifier()));
            throw new ComponentConfigurationException("Missing required annotation @" + annotationClass.getSimpleName() + " on class " + this.getClass().getName() + " or its hierarchy.", getIdentifier());
        }

        return annotation;
    }

}
