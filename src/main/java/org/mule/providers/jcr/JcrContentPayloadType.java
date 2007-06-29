package org.mule.providers.jcr;

/**
 * TODO comment
 *
 * @author David Dossot (david@dossot.net)
 */
final class JcrContentPayloadType {

    public static final JcrContentPayloadType NONE =
            new JcrContentPayloadType("none");

    public static final JcrContentPayloadType NO_BINARY =
            new JcrContentPayloadType("nobinary");

    public static final JcrContentPayloadType FULL =
            new JcrContentPayloadType("full");

    private static final JcrContentPayloadType[] ALL_TYPES =
            new JcrContentPayloadType[] { NONE, NO_BINARY, FULL };

    private final String contentPayloadType;

    private JcrContentPayloadType(final String contentPayloadType) {
        this.contentPayloadType = contentPayloadType;
    }

    public String toString() {
        return contentPayloadType;
    }

    public int hashCode() {
        return contentPayloadType.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj instanceof JcrContentPayloadType) {
            return contentPayloadType.equals(((JcrContentPayloadType) obj).contentPayloadType);
        } else {
            return false;
        }
    }

    public static JcrContentPayloadType fromString(String type)
            throws IllegalArgumentException {

        if ((type == null) || (type.trim().equals(""))) {
            return NONE;
        }

        if ((type != null) && (!type.equals(""))) {
            for (int i = 0; i < ALL_TYPES.length; i++) {
                JcrContentPayloadType contentPayload = ALL_TYPES[i];

                if (contentPayload.contentPayloadType.equalsIgnoreCase(type)) {
                    return contentPayload;
                }
            }
        }

        throw new IllegalArgumentException("Invalid content payload type: "
                + type);
    }

}
