package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.IntegrationComponent;
import integration.core.messaging.component.handler.filter.AcceptancePolicy;
import integration.messaging.hl7.component.adapter.mllp.BaseMllpOutboundAdapter;

@IntegrationComponent(name = "To-Melbourne-Hospital-MLLP-Outbound-Adapter")
@AcceptancePolicy(name = "acceptAllMessages")
@AllowedContentType(ContentTypeEnum.HL7)
public class MelbourneHospitalMLLPOutboundAdapter extends BaseMllpOutboundAdapter {

}
