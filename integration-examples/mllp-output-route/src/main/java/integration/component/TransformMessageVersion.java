package integration.component;

import org.springframework.stereotype.Component;

import integration.messaging.component.processingstep.transformation.TransformationException;
import integration.messaging.hl7.component.processingstep.transformation.ChangeMessageVersionTransformer;

/**
 * 
 * Updates the message version to 2.5
 * 
 * @author Brendan Douglas
 *
 */
@Component("changeVersionTo2.5")
public class TransformMessageVersion extends ChangeMessageVersionTransformer {

    @Override
    public String getNewVersion() throws TransformationException {
        return "2.5";
    }
}
