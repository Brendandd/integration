package integration.core.domain.messaging;

/**
 * The types of message flow status types
 */
public enum MessageFlowStepActionType {
    RECEIVED_BY_INBOUND_ADAPTER,
    ACCEPTED,
    FILTERED,
    ACK_SENT,
    OUTBOUND_MESSAGE_HANDLING_COMPLETE;
}
