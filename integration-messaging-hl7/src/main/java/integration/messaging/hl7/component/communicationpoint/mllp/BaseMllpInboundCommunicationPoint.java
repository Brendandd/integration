package integration.messaging.hl7.component.communicationpoint.mllp;

import static org.apache.camel.component.hl7.HL7.ack;

import org.apache.camel.builder.TemplatedRouteBuilder;
import org.springframework.context.annotation.DependsOn;

import integration.messaging.component.communicationpoint.BaseInboundCommunicationPoint;

/**
 * Base class for all MLLP/HL7 inbound communication points. This components reads the
 * HL7 message, stores it, writes an event and returns an ACK to the sender.
 * 
 * @author Brendan Douglas
 *
 */
public abstract class BaseMllpInboundCommunicationPoint extends BaseInboundCommunicationPoint {

    public BaseMllpInboundCommunicationPoint(String componentName) throws Exception {
        super(componentName);
    }

    private static final String CONTENT_TYPE = "HL7";

    public String getHost() {
        return componentProperties.get("HOST");
    }

    public String getPort() {
        return componentProperties.get("PORT");
    }

    @Override
    public String getFromUriString() {
        String target = getHost() + ":" + getPort();
        return "netty:tcp://" + target + getOptions();
    }

    @Override
    public String getOptions() {
        return "?sync=true&encoders=#hl7encoder&decoders=#hl7decoder";
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    @DependsOn("routeTemplates")
    public void configure() throws Exception {
        super.configure();

        // A route to receive a HL7 message via MLLP, store the message, store an event and generate and send the ACK all
        // within a single transaction.  This is the initial entry point for a HL7 message.
        from(getFromUriString()).routeId("messageReceiver-" + identifier.getComponentPath())
            .setHeader("contentType", constant(getContentType()))
            .routeGroup(identifier.getComponentPath()).autoStartup(isInboundRunning).transacted()
            .bean(messageProcessor, "storeInboundMessageFlowStep(*," + identifier.getComponentRouteId() + ")")
            .transform(ack())
            .bean(messageProcessor, "recordAck(*," + identifier.getComponentRouteId() + ")")
            .bean(messageProcessor, "recordInboundProcessingCompleteEvent(*)");
        
                
        // Outbound processor for a HL7/MLLP inbound communication point.  This route will either create an event for further processing by other components or filter
        // the message.  No other processing is done here.
        TemplatedRouteBuilder.builder(camelContext, "inboundCommunicationPointOutboundProcessorTemplate")
            .parameter("isOutboundRunning", isOutboundRunning).parameter("componentPath", identifier.getComponentPath())
            .parameter("componentRouteId", identifier.getComponentRouteId())
            .parameter("contentType", getContentType())
            .bean("messageForwardingPolicy", getMessageForwardingPolicy())
            .add();
        
        
        // Add the message flow step id to the outbound processing complete topic so it can be picked up by one or more other components.  This is the final
        // step in HL7/MLLP inbound communication points.
        TemplatedRouteBuilder.builder(camelContext, "addToOutboundProcessingCompleteTopicTemplate")
            .parameter("componentPath", identifier.getComponentPath())
            .add();
    }
}
