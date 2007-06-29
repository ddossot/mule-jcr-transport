/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr;

import java.util.Collection;

import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;
import org.mule.util.CollectionUtils;

/**
 * <code>JcrMessageAdapter</code> TODO document
 */
public final class JcrMessageAdapter extends AbstractMessageAdapter {
    private static final long serialVersionUID = 2337091822007161288L;

    private final Collection payload;

    private final byte[] payloadBytes;

    public JcrMessageAdapter(Object message) throws MessagingException {
        if (message instanceof Collection) {
            this.payload = (Collection) message;

            // validate the collection is homogeneous
            try {
                CollectionUtils.typedCollection(payload, SerializableJcrEvent.class);
            } catch (IllegalArgumentException iae) {
                throw new MessageTypeNotSupportedException(message, getClass(),
                        iae);
            }

            payloadBytes = message.toString().getBytes();

        } else {
            throw new MessageTypeNotSupportedException(message, getClass());
        }

    }

    public String getPayloadAsString(String encoding) throws Exception {
        return new String(getPayloadAsBytes(), encoding);
    }

    public byte[] getPayloadAsBytes() throws Exception {
        return payloadBytes;
    }

    public Object getPayload() {
        return payload;
    }

}
