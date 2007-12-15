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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.jcr.observation.EventIterator;

import org.mule.config.i18n.CoreMessages;
import org.mule.impl.ThreadSafeAccess;
import org.mule.providers.AbstractMessageAdapter;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.MessageTypeNotSupportedException;
import org.mule.umo.transformer.TransformerException;
import org.mule.util.IOUtils;

/**
 * <code>JcrMessageAdapter</code> allows a <code>MuleEvent</code> to access
 * the properties and payload of a JCR Event in a uniform way. The
 * <code>JcrMessageAdapter</code> expects a message of type
 * <i>java.util.Collection</i> that only contains objects of type
 * <code>SerializableJcrEvent</code>. It will throw an
 * IllegalArgumentException if the source message type is not compatible.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrMessageAdapter extends AbstractMessageAdapter {
	private static final long serialVersionUID = 2337091822007161288L;

	private final Object payload;

	private byte[] contents = null;

	public JcrMessageAdapter(Object message) throws MessagingException {
		if ((message instanceof Serializable)
				|| (message instanceof InputStream)
				|| (message instanceof EventIterator)) {

			payload = message;

		} else {
			throw new MessageTypeNotSupportedException(message, getClass());
		}

	}

	protected JcrMessageAdapter(JcrMessageAdapter template) {
		super(template);
		payload = template.payload;
		contents = template.contents;
	}

	public ThreadSafeAccess newThreadCopy() {
		return new JcrMessageAdapter(this);
	}

	protected byte[] convertToBytes(Object object) throws TransformerException,
			UnsupportedEncodingException {

		assertAccess(READ);

		if (object instanceof InputStream) {
			try {
				return IOUtils.toByteArray((InputStream) object);
			} catch (IOException ioe) {
				throw new TransformerException(CoreMessages.transformFailed(
						object.getClass().getName(), "InputStream"), ioe);
			}
		} else if (object instanceof EventIterator) {
			// trying to get a bytes representation of the iterator is an error
			return super.convertToBytes(EventIterator.class.getName());
		} else {
			return super.convertToBytes(object);
		}
	}

	public byte[] getPayloadAsBytes() throws Exception {
		assertAccess(READ);

		synchronized (this) {
			contents = convertToBytes(payload);
			return contents;
		}
	}

	public String getPayloadAsString(String encoding) throws Exception {
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
