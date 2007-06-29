package org.mule.providers.jcr;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.observation.Event;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.util.Base64;
import org.mule.util.IOUtils;

/**
 * Provides a serializable, disconnected and fully resolved JCR event
 * representation. JCR events are often RMI stubs hence not fitted to be carried
 * in messages payloads. This class also translates event types from numeric to
 * plain English and supports the notion of event content, that is mising in the
 * JCR event.
 *
 * @author David Dossot (david@dossot.net)
 */
final class JcrEvent implements SerializableJcrEvent {
    private static final long serialVersionUID = -7200906980423201081L;

    protected static final Log logger =
            LogFactory.getLog(JcrEvent.class);

    private final String path;

    private final String type;

    private final String userID;

    private final Object content;

    private JcrEvent(final String path, final String type,
            final String userID, final Object content) {

        this.path = path;
        this.type = type;
        this.userID = userID;
        this.content = content;
    }

    static SerializableJcrEvent newInstance(Event event, Session session,
            JcrContentPayloadType contentPayloadType)
            throws RepositoryException {

        return new JcrEvent(event.getPath(),
                getEventTypeNameFromValue(event.getType()), event.getUserID(),
                getEventContent(event, session, contentPayloadType));
    }

    private static Object getEventContent(Event event, Session session,
            JcrContentPayloadType contentPayloadType)
            throws RepositoryException {

        Object result = "";

        if (!JcrContentPayloadType.NONE.equals(contentPayloadType)) {

            String eventPath = event.getPath();
            int eventType = event.getType();

            // tentatively add content from the path of the event if the
            // event is not a removal if the content can not be fetched (because
            // it has changed between the moment the event was raised and the
            // moment we build this message), report the error at info level
            // only (this is a failure that can happen and is not business
            // critical in any way).
            if ((eventType == Event.PROPERTY_ADDED)
                    || (eventType == Event.PROPERTY_CHANGED)) {

                try {
                    Item item = session.getItem(eventPath);

                    if (!item.isNode()) {
                        result =
                                outputProperty((Property) item,
                                        contentPayloadType);
                    }

                } catch (Exception ignoredException) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Can not fetch content for event path: "
                                + eventPath + "("
                                + ignoredException.getMessage() + ")");
                    }
                }
            }
        }

        return result;
    }

    private static Object outputProperty(Property property,
            JcrContentPayloadType contentPayloadType)
            throws RepositoryException, ValueFormatException {

        Object result;

        if (property.getDefinition().isMultiple()) {
            List contentList = new ArrayList();

            Value[] propertyValues = property.getValues();

            for (int i = 0; i < propertyValues.length; i++) {
                contentList.add(outputPropertyValue(property,
                        propertyValues[i], contentPayloadType));
            }

            result = contentList;
        } else {
            result =
                    outputPropertyValue(property, property.getValue(),
                            contentPayloadType);
        }

        return result;
    }

    private static String outputPropertyValue(Property property,
            Value propertyValue, JcrContentPayloadType contentPayloadType) {

        String result = "";

        try {
            int propertyType = propertyValue.getType();

            if (propertyType == PropertyType.BINARY) {
                if (!JcrContentPayloadType.NO_BINARY.equals(contentPayloadType)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    IOUtils.copy(property.getStream(), new Base64.OutputStream(
                            baos));

                    result = baos.toString();
                }
            } else {
                result = propertyValue.getString();
            }
        } catch (Exception e) {
            String propertyPath = "N/A";

            try {
                propertyPath = property.getPath();
            } catch (RepositoryException re) {
                propertyPath = re.getMessage();
            } finally {
                // log error but do not break message building
                logger.error("Can not fetch property value for: "
                        + propertyPath, e);
            }
        }

        return result;
    }

    // This should really be in JCR API!
    private static String getEventTypeNameFromValue(int eventType) {
        switch (eventType) {

        case Event.NODE_ADDED:
            return "NODE_ADDED";

        case Event.NODE_REMOVED:
            return "NODE_REMOVED";

        case Event.PROPERTY_ADDED:
            return "PROPERTY_ADDED";

        case Event.PROPERTY_CHANGED:
            return "PROPERTY_CHANGED";

        case Event.PROPERTY_REMOVED:
            return "PROPERTY_REMOVED";

        default:
            return "UNKNOWN";
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content.toString();
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the userID
     */
    public String getUserID() {
        return userID;
    }

}
