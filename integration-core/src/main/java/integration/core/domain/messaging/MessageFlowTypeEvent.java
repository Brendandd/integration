package integration.core.domain.messaging;

/**
 * Types of message flow events.
 * 
 * @author Brendan Douglas
 */
public enum MessageFlowTypeEvent {
    COMPONENT_OUTBOUND_PROCESSING_COMPLETE, MESSAGE_READY_FOR_SENDING, COMPONENT_INBOUND_PROCESSING_COMPLETE,
    ROUTE_OUTBOUND_CONNECTOR_COMPLETE;
}
