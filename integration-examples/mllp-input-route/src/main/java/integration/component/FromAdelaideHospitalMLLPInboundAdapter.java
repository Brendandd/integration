package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.runtime.messaging.component.AllowedContentType;
import integration.core.runtime.messaging.component.annotation.IntegrationComponent;
import integration.messaging.hl7.component.adapter.mllp.BaseMllpInboundAdapter;

/**
 * An MLLP inbound adapter.
 * 
 * @author Brendan Douglas
 */
@IntegrationComponent(name = "From-Adelaide-Hospital-MLLP-Inbound-Adapter")
@AllowedContentType(ContentTypeEnum.HL7)
public class FromAdelaideHospitalMLLPInboundAdapter extends BaseMllpInboundAdapter {

}
