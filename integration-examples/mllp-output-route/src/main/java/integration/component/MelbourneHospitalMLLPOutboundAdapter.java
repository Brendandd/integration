package integration.component;

import integration.core.domain.configuration.ContentTypeEnum;
import integration.core.messaging.component.AllowedContentType;
import integration.core.messaging.component.annotation.IntegrationComponent;
import integration.messaging.hl7.component.adapter.mllp.BaseMllpOutboundAdapter;

@IntegrationComponent(name = "To-Melbourne-Hospital-MLLP-Outbound-Adapter")
@AllowedContentType(ContentTypeEnum.HL7)
public class MelbourneHospitalMLLPOutboundAdapter extends BaseMllpOutboundAdapter {

}
