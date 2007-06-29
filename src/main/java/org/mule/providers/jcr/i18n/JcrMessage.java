package org.mule.providers.jcr.i18n;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;

/**
 * TODO comment
 */
public abstract class JcrMessage extends MessageFactory {

    private static final String BUNDLE_PATH = getBundlePath("jcr");

    public static Message missingDependency(String name) {
        return createMessage(BUNDLE_PATH, 0, name);
    }

    public static Message observationsNotSupported() {
        return createMessage(BUNDLE_PATH, 1);
    }

    public static Message canNotGetObservationManager(String workspaceName) {
        return createMessage(BUNDLE_PATH, 2, workspaceName);
    }

    public static Message noSaxTransformer() {
        return createMessage(BUNDLE_PATH, 3);
    }
}
