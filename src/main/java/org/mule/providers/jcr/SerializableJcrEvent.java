package org.mule.providers.jcr;

import java.io.Serializable;

/**
 * TODO comment
 *
 * @author David Dossot (david@dossot.net)
 */
public interface SerializableJcrEvent extends Serializable {

    /**
     * @return the content
     */
    String getContent();

    /**
     * @return the path
     */
    String getPath();

    /**
     * @return the type
     */
    String getType();

    /**
     * @return the userID
     */
    String getUserID();

}