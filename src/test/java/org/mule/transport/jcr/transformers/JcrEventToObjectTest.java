/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr.transformers;

import java.util.Collections;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import org.apache.jackrabbit.commons.iterator.EventIteratorAdapter;
import org.mule.api.transformer.Transformer;
import org.mule.transformer.AbstractTransformerTestCase;
import org.mule.transport.jcr.JcrContentPayloadType;
import org.mule.transport.jcr.JcrEventTestCase;
import org.mule.transport.jcr.JcrMessageReceiver;
import org.mule.transport.jcr.JcrMessageReceiverContext;
import org.mule.transport.jcr.JcrUtils;
import org.mule.transport.jcr.RepositoryTestSupport;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrEventToObjectTest extends AbstractTransformerTestCase {
	private Event testJcrEvent;

	@Override
	public Object getResultData() {
		try {
			return Collections.singletonList(JcrUtils.newJcrMessage(
					testJcrEvent, JcrMessageReceiver
							.getJcrMessageReceiverContext()
							.getObservingSession(), JcrMessageReceiver
							.getJcrMessageReceiverContext()
							.getContentPayloadType()));

		} catch (final RepositoryException re) {
			throw new RuntimeException(re);
		}
	}

	@Override
	public Transformer getRoundTripTransformer() throws Exception {
		return null;
	}

	@Override
	public Object getTestData() {
		JcrMessageReceiver
				.setJcrMessageReceiverContext(new JcrMessageReceiverContext() {
					public JcrContentPayloadType getContentPayloadType() {
						return JcrContentPayloadType.NO_BINARY;
					}

					public Session getObservingSession() {
						return RepositoryTestSupport.getSession();
					}
				});

		testJcrEvent = new JcrEventTestCase.DummyEvent("/",
				Event.PROPERTY_CHANGED, "foo");

		return new EventIteratorAdapter(Collections.singleton(testJcrEvent));
	}

	@Override
	public Transformer getTransformer() throws Exception {
		return new JcrEventToObject();
	}

}
