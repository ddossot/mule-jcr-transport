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

import org.mule.MuleManager;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.tck.providers.AbstractMessageReceiverTestCase;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMODescriptor;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.provider.UMOMessageReceiver;

import com.mockobjects.dynamic.Mock;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageReceiverTestCase extends AbstractMessageReceiverTestCase {

	public UMOMessageReceiver getMessageReceiver() throws Exception {
		Mock mockComponent = new Mock(UMOComponent.class);
		Mock mockDescriptor = new Mock(UMODescriptor.class);
		mockComponent.expectAndReturn("getDescriptor", mockDescriptor.proxy());
		mockDescriptor.expectAndReturn("getResponseTransformer", null);

		return new JcrMessageReceiver(endpoint.getConnector(),
				(UMOComponent) mockComponent.proxy(), endpoint);
	}

	public UMOEndpoint getEndpoint() throws Exception {
		MuleManager.getInstance().registerConnector(new JcrConnector());
		return new MuleEndpoint("jcr://path/to/observedNode", true);
	}

	public void testReceiverProperties() throws Exception {
		JcrMessageReceiver messageReceiver = (JcrMessageReceiver) getMessageReceiver();

		assertEquals("/path/to/observedNode", messageReceiver.getAbsPath());

		JcrConnector connector = (JcrConnector) messageReceiver.getConnector();

		assertEquals(connector.getContentPayloadType(), messageReceiver
				.getContentPayloadType().toString());

		assertEquals(connector.getEventTypes(), messageReceiver.getEventTypes());

		assertEquals(connector.isDeep(), messageReceiver.isDeep());

		assertEquals(connector.getUuid(), messageReceiver.getUuid());

		assertEquals(connector.getNodeTypeName(), messageReceiver
				.getNodeTypeName());

		assertEquals(connector.isNoLocal(), messageReceiver.isNoLocal());
	}

}
