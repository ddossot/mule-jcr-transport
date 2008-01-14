/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr.transformers;

import java.util.Collections;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import org.apache.jackrabbit.commons.iterator.EventIteratorAdapter;
import org.mule.providers.jcr.JcrContentPayloadType;
import org.mule.providers.jcr.JcrEventTestCase;
import org.mule.providers.jcr.JcrMessageReceiver;
import org.mule.providers.jcr.JcrMessageReceiverContext;
import org.mule.providers.jcr.JcrMessageUtils;
import org.mule.providers.jcr.RepositoryTestSupport;
import org.mule.tck.AbstractTransformerTestCase;
import org.mule.umo.transformer.UMOTransformer;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrEventToObjectTest extends AbstractTransformerTestCase {
	private Event testJcrEvent;

	public Object getResultData() {
		try {
			return Collections.singletonList(JcrMessageUtils.newInstance(
					testJcrEvent, JcrMessageReceiver
							.getJcrMessageReceiverContext()
							.getObservingSession(), JcrMessageReceiver
							.getJcrMessageReceiverContext()
							.getContentPayloadType()));

		} catch (RepositoryException re) {
			throw new RuntimeException(re);
		}
	}

	public UMOTransformer getRoundTripTransformer() throws Exception {
		return null;
	}

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

	public UMOTransformer getTransformer() throws Exception {
		return new JcrEventToObject();
	}

}
