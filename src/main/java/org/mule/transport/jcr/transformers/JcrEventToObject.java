/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr.transformers;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;
import org.mule.transport.jcr.JcrContentPayloadType;
import org.mule.transport.jcr.JcrMessageReceiver;
import org.mule.transport.jcr.JcrUtils;

/**
 * Transforms a JCR <code>EventIterator</code> into an object that can be used
 * as a payload.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrEventToObject extends AbstractTransformer {

	private JcrContentPayloadType contentPayloadType;

	public JcrEventToObject() {
		super();
		registerSourceType(EventIterator.class);
	}

	@Override
	protected Object doTransform(final Object src, final String encoding)
			throws TransformerException {

		final List eventList = new ArrayList();
		final EventIterator eventIterator = (EventIterator) src;

		JcrMessageReceiverContext jcrMessageReceiverContext = JcrMessageReceiver
				.getJcrMessageReceiverContext();

		while (eventIterator.hasNext()) {
			try {
				eventList.add(JcrUtils.newJcrMessage(eventIterator.nextEvent(),
						jcrMessageReceiverContext.getObservingSession(),
						contentPayloadType != null ? contentPayloadType
								: jcrMessageReceiverContext
										.getContentPayloadType()));

			} catch (final RepositoryException re) {
				logger.error("Can not process JCR event", re);
			}
		}

		return eventList;
	}

	public void setContentPayloadType(final String contentPayloadType) {
		this.contentPayloadType = JcrContentPayloadType
				.fromString(contentPayloadType);
	}

}
