package integration.core.domain.messaging;

import java.util.Map;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.util.Utils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A message.
 * 
 * @author Brendan Douglas
 *
 */
@Entity
@Table(name = "message")
public class Message extends BaseIntegrationDomain {
    private String content;
    private String headers;
    private String contentType;

    public Message() {

    }

    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Column(name = "headers")
    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return content;
    }

    @Column(name = "content_type")
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Adds a header to this message.
     * 
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        Map<String, Object> headers = Utils.convertFromJSON(this.headers);
        headers.put(key, value);

        setHeaders(Utils.convertToJSON(headers));
    }

    /**
     * Removes a header from this message.
     * 
     * @param key
     */
    public void removeHeader(String key) {
        Map<String, Object> headers = Utils.convertFromJSON(this.headers);

        if (headers.containsKey(key)) {
            headers.remove(key);
            setHeaders(Utils.convertToJSON(headers));
        }
    }

    /**
     * Gets a header from this message if it exists. Null if it doesn't exist.
     * 
     * @param key
     */
    public String getHeader(String key) {
        Map<String, Object> headers = Utils.convertFromJSON(this.headers);

        return (String) headers.get(key);
    }

    /**
     * Checks to see if a header exists on this message.
     * 
     * @param key
     * @return
     */
    public boolean doesHeaderExist(String key) {
        Map<String, Object> headers = Utils.convertFromJSON(this.headers);

        return headers.containsKey(key);
    }
}
