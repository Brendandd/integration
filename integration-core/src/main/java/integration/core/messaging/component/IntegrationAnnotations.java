package integration.core.messaging.component;

import java.lang.annotation.Annotation;
import java.util.Set;

import integration.core.messaging.IntegrationRoute;
import integration.core.messaging.component.adapter.AdapterOption;
import integration.core.messaging.component.adapter.AdapterOptions;
import integration.core.messaging.component.connector.FromRoute;
import integration.core.messaging.component.connector.ToRoute;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;
import integration.core.messaging.component.handler.filter.ForwardingPolicy;
import integration.core.messaging.component.handler.splitter.UsesSplitter;
import integration.core.messaging.component.handler.transformation.UsesTransformer;

/**
 * A set of all the allowed custom annotations.
 */
public class IntegrationAnnotations {
    
    protected static final Set<Class<? extends Annotation>> ALL_INTEGRATION_ANNOTATIONS = Set.of(
        AllowedContentType.class,
        UsesTransformer.class,
        UsesSplitter.class,
        ForwardingPolicy.class,
        AcceptancePolicy.class,
        AdapterOption.class,
        AdapterOptions.class,
        IntegrationComponent.class,
        IntegrationRoute.class,
        FromRoute.class,
        ToRoute.class
    );
}
