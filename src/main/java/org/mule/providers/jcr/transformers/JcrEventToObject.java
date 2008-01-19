/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr.transformers;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;

import org.mule.providers.jcr.JcrContentPayloadType;
import org.mule.providers.jcr.JcrMessageReceiver;
import org.mule.providers.jcr.JcrMessageReceiverContext;
import org.mule.providers.jcr.JcrUtils;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

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

	protected Object doTransform(Object src, String encoding)
			throws TransformerException {

		List eventList = new ArrayList();
		EventIterator eventIterator = (EventIterator) src;

		JcrMessageReceiverContext jcrMessageReceiverContext = JcrMessageReceiver
				.getJcrMessageReceiverContext();

		while (eventIterator.hasNext()) {
			try {
				eventList.add(JcrUtils.newJcrMessage(eventIterator
						.nextEvent(), jcrMessageReceiverContext
						.getObservingSession(),
						contentPayloadType != null ? contentPayloadType
								: jcrMessageReceiverContext
										.getContentPayloadType()));

			} catch (RepositoryException re) {
				logger.error("Can not process JCR event", re);
			}
		}

		return eventList;
	}

	public void setContentPayloadType(String contentPayloadType) {
		this.contentPayloadType = JcrContentPayloadType
				.fromString(contentPayloadType);
	}

}
