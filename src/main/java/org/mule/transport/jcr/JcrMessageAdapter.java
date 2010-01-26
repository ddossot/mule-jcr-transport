/*
 * $Id: MessageAdapter.vm 10787 2008-02-12 18:51:50Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.jcr.observation.EventIterator;

import org.apache.commons.lang.SerializationUtils;
import org.mule.api.ThreadSafeAccess;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.MessageTypeNotSupportedException;
import org.mule.config.i18n.CoreMessages;
import org.mule.transport.AbstractMessageAdapter;
import org.mule.util.IOUtils;

/**
 * <code>JcrMessageAdapter</code> allows a <code>MuleEvent</code> to access the
 * properties and payload of a JCR Event in a uniform way. The
 * <code>JcrMessageAdapter</code> expects a message of type
 * <i>java.util.Collection</i> that only contains objects of type
 * <code>SerializableJcrEvent</code>. It will throw an IllegalArgumentException
 * if the source message type is not compatible.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrMessageAdapter extends AbstractMessageAdapter {
    private static final long serialVersionUID = 2337091822097161288L;

    private final Object payload;

    private byte[] contents;

    public JcrMessageAdapter(final Object message) throws MessageTypeNotSupportedException {
        if ((message instanceof Serializable) || (message instanceof InputStream) || (message instanceof EventIterator)) {

            payload = message;

        } else {
            throw new MessageTypeNotSupportedException(message, getClass());
        }

    }

    protected JcrMessageAdapter(final JcrMessageAdapter template) {
        super(template);
        payload = template.payload;
        contents = template.contents;
    }

    @Override
    public ThreadSafeAccess newThreadCopy() {
        return new JcrMessageAdapter(this);
    }

    protected byte[] convertToBytes(final Object object) throws TransformerException, UnsupportedEncodingException {

        assertAccess(READ);

        if (object instanceof InputStream) {
            return IOUtils.toByteArray((InputStream) object);

        } else if (object instanceof EventIterator) {
            // trying to get a bytes representation of the iterator is an error
            return EventIterator.class.getName().getBytes(getEncoding());

        } else if (object instanceof String) {
            return object.toString().getBytes(getEncoding());

        } else if (object instanceof byte[]) {
            return (byte[]) object;

        } else if (object instanceof Serializable) {
            try {
                return SerializationUtils.serialize((Serializable) object);
            } catch (final Exception e) {
                throw new TransformerException(CoreMessages.transformFailed(object.getClass().getName(), "byte[]"), e);
            }
        } else {
            throw new TransformerException(CoreMessages.transformFailed(object.getClass().getName(), "byte[] or "
                    + Serializable.class.getName()));
        }
    }

    public byte[] getPayloadAsBytes() throws Exception {
        assertAccess(READ);

        synchronized (this) {
            if (contents == null) {
                contents = convertToBytes(payload);
            }

            return contents;
        }
    }

    public String getPayloadAsString(final String encoding) throws Exception {
        assertAccess(READ);

        synchronized (this) {
            return new String(this.getPayloadAsBytes(), encoding);
        }
    }

    public Object getPayload() {
        assertAccess(READ);
        return payload;
    }

}
