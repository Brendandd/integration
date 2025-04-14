package integration.messaging.hl7.component.communicationpoint.mllp;

import org.apache.camel.builder.TemplatedRouteBuilder;

import integration.messaging.component.communicationpoint.BaseOutboundCommunicationPoint;

/**
 * Base class for all MLLP/HL7 Outbound communication points.
 */
public abstract class BaseMllpOutboundCommunicationPoint extends BaseOutboundCommunicationPoint {

    public BaseMllpOutboundCommunicationPoint(String componentName) {
        super(componentName);
    }

    private static final String CONTENT_TYPE = "HL7";

    @Override
    public String getOptions() {
        return "?sync=true&encoders=#hl7encoder&decoders=#hl7decoder";
    }

    public String getTargetHost() {
        return componentProperties.get("TARGET_HOST");
    }

    public String getTargetPort() {
        return componentProperties.get("TARGET_PORT");
    }

    @Override
    public String getToUriString() {
        String target = getTargetHost() + ":" + getTargetPort();
        return "netty:tcp://" + target + getOptions();
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void configure() throws Exception {
        super.configure();
        
        
        // Creates one or more routes based on this components source components.  Each route reads from a topic.  This is the entry point for a MLLP/HL7 outbound
        // communication point.
        for (String sourceComponent : sourceComponentPaths) {
            TemplatedRouteBuilder.builder(camelContext, "componentInboundTopicConsumerTemplate")
                .parameter("isInboundRunning", isInboundRunning).parameter("componentPath", identifier.getComponentPath())
                .parameter("sourceComponentPath", sourceComponent)
                .parameter("componentRouteId", identifier.getComponentRouteId())
                .parameter("contentType", constant(getContentType()))
                .bean("messageAcceptancePolicy", getMessageAcceptancePolicy())
                .add();
        }
        
       
        // A route to write an event record indicating the message is ready for sending to the destination.
        from("direct:outboundProcessor-" + identifier.getComponentPath())
            .routeId("outboundProcessor-" + identifier.getComponentPath()).routeGroup(identifier.getComponentPath())
            .autoStartup(isOutboundRunning)
            .setHeader("contentType", simple(getContentType()))
            .bean(messageProcessor, "recordMessageReadyForSendingEvent(*)");

        
        // Sends the message to the final destination.  This is called from a transaction outbox process so we are guaranteed the message will be sent  //TODO handle ACKs received by the destination.
        from("direct:sendMessageToDestination-" + identifier.getComponentPath())
            .routeId("sendMessageToDestination-" + identifier.getComponentPath())
            .routeGroup(identifier.getComponentPath())
            .transacted()
                .bean(messageProcessor, "deleteMessageFlowEvent(*)")
                .transform().method(messageProcessor, "replaceMessageBodyIdWithMessageContent(*)")
                .bean(messageProcessor, "storeOutboundMessageFlowStep(*," + identifier.getComponentRouteId() + ")")
                .to(getToUriString());
    }
}
