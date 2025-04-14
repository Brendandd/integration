package integration.messaging.hl7.component.communicationpoint.mllp;

import org.apache.camel.component.hl7.HL7MLLPNettyDecoderFactory;
import org.apache.camel.component.hl7.HL7MLLPNettyEncoderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Config for MLLP.
 */
@Component
public class MllpBeans {

    @Bean
    public HL7MLLPNettyDecoderFactory hl7decoder() {
        return new HL7MLLPNettyDecoderFactory();
    }

    @Bean
    public HL7MLLPNettyEncoderFactory hl7encoder() {
        return new HL7MLLPNettyEncoderFactory();
    }

}
