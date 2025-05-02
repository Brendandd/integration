package integration.core.domain.configuration;

/**
 * The types of components.
 */
public enum ComponentType {
    INBOUND_DIRECTORY_ADAPTER,
    OUTBOUND_DIRECTORY_ADAPTER,
    
    INBOUND_MLLP_ADAPTER,
    OUTBOUND_MLLP_ADAPTER,
    
    INBOUND_ROUTE_CONNECTOR,
    OUTBOUND_ROUTE_CONNECTOR,
    
    TRANSFORMER,
    FILTER,
    SPLITTER;
}
