package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.annotation.IntegrationComponent;
import integration.core.messaging.component.type.handler.filter.BaseFilterProcessingStep;
import integration.core.messaging.component.type.handler.filter.annotation.AcceptancePolicy;

/**
 * HL7 message type filter compoment.
 * 
 * @author Brendan Douglas
 * 
 */
@IntegrationComponent(name = "Allow-only-ADT-A04")
@AcceptancePolicy(name = "acceptADT^A04")
@AllowedContentType(ContentTypeEnum.HL7)
public class Hl7MessageTypeFilter extends BaseFilterProcessingStep {

}
