package integration.messaging.hl7.component.handler.filter;

import integration.messaging.component.handler.filter.FilterException;
import integration.messaging.component.handler.filter.MessageAcceptancePolicy;
import integration.messaging.hl7.datamodel.HL7Message;

/**
 * Base class for all HL7 message filters. A filter either accepts or rejects a
 * message.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseHL7MessageAcceptancePolicy extends MessageAcceptancePolicy {

    @Override
    public boolean applyPolicy(String messageContent) throws FilterException {
        HL7Message hl7Message = new HL7Message(messageContent);

        return applyPolicy(hl7Message);
    }

    /**
     * Does the actual transformation.
     * 
     * @param source
     * @return
     */
    public abstract boolean applyPolicy(HL7Message source) throws FilterException;
}
