package integration.component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import integration.core.runtime.messaging.component.type.adapter.smb.annotation.FileNamingStrategy;
import integration.core.runtime.messaging.exception.MessageFlowException;

/**
 * A custom file naming strategy which appends a date time to the original file name.
 */
@Component("customNamingStrategy")
public class CustomFileNamingStrategy extends FileNamingStrategy {

    @Override
    public String getFilename(Exchange exchange, long messageFlowId) throws MessageFlowException {
        String filename = propertyService.getPropertyValue("CamelFileName", messageFlowId);
        
        if (filename == null) {
            return null;
        }
        
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        int dotIndex = filename.lastIndexOf('.');
        String newFilename;
        
        if (dotIndex != -1) {
            String namePart = filename.substring(0, dotIndex);
            String extension = filename.substring(dotIndex);
            newFilename = namePart + "-" + now + extension;
        } else {
            newFilename = filename + "-" + now;
        }

        return newFilename;
    }
}
