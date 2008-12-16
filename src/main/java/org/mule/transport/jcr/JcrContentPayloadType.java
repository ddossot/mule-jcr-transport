/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import org.mule.util.StringUtils;

/**
 * A typesafe enum that represents the supported content paylod types for a JCR
 * event.
 * 
 * @author David Dossot (david@dossot.net)
 */
public enum JcrContentPayloadType {

    /**
     * No content will be fetched from JCR: the payload will be limited to the
     * event information.
     */
    NONE("none"),

    /**
     * The payload will contain event information and data from the node source
     * of the event, only if this data is not of bynary type.
     */
    NO_BINARY("nobinary"),

    /**
     * The payload will contain event information and data from the node source
     * of the event.
     */
    FULL("full");

    private final String name;

    private JcrContentPayloadType(final String contentPayloadType) {
        this.name = contentPayloadType;
    }

    @Override
    public String toString() {
        return name;
    }

    public static JcrContentPayloadType fromString(final String type)
            throws IllegalArgumentException {

        if (StringUtils.isBlank(type)) {
            return NONE;
        }

        for (final JcrContentPayloadType contentPayloadType : values()) {
            if (contentPayloadType.name.equalsIgnoreCase(type)) {
                return contentPayloadType;
            }
        }

        throw new IllegalArgumentException("Invalid content payload type: "
                + type);
    }

}
