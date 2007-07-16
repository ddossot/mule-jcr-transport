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

import java.util.Collections;
import java.util.List;

import org.mule.MuleManager;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.umo.endpoint.UMOEndpoint;

/**
 * @author David Dossot
 */
public class JcrMessageReceiverOverridesTestCase extends
		JcrMessageReceiverTestCase {

	private static final List UUID_LIST = Collections.singletonList("uuid_01");

	private static final List NODE_TYPE_NAME = Collections
			.singletonList("node_type_01");

	public UMOEndpoint getEndpoint() throws Exception {
		MuleManager.getInstance().registerConnector(new JcrConnector());

		MuleEndpoint muleEndpoint = new MuleEndpoint(
				"jcr://path/to/observedNode?contentPayloadType=full&eventTypes=5&deep=true&noLocal=false",
				true);

		muleEndpoint.setProperty("uuid", UUID_LIST);
		muleEndpoint.setProperty("nodeTypeName", NODE_TYPE_NAME);

		return muleEndpoint;
	}

	public void testReceiverProperties() throws Exception {
		JcrMessageReceiver messageReceiver = (JcrMessageReceiver) getMessageReceiver();
		
		assertEquals("/path/to/observedNode", messageReceiver.getAbsPath());

		assertEquals(JcrContentPayloadType.FULL, messageReceiver
				.getContentPayloadType());

		assertEquals(new Integer(5), messageReceiver.getEventTypes());

		assertEquals(Boolean.TRUE, messageReceiver.isDeep());

		assertEquals(UUID_LIST, messageReceiver.getUuid());

		assertEquals(NODE_TYPE_NAME, messageReceiver.getNodeTypeName());

		assertEquals(Boolean.FALSE, messageReceiver.isNoLocal());
	}

}
