package integration.core.runtime.messaging.component;

import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;

import integration.core.runtime.messaging.service.MessageFlowService;

/**
 * Base processor for all camel processors called during message flows.
 * 
 * @param <T>
 */
public abstract class BaseMessageFlowProcessor<T extends MessagingComponent> implements Processor {
    
    @Autowired
    protected MessageFlowService messageFlowService;
    
    protected T component;

    
    public void setComponent(T component) {
        this.component = component;
    }
}
