package integration.core.runtime.messaging.component.type.connector;

import org.apache.camel.Exchange;

/**
 * Allows an outbound route connector destination to be dynamic instead of static.  This
 * allows routing based on properties and/or message content.
 */
public interface DynamicDestinationResolver {
    String resolveDestination(Exchange exchange);
}
