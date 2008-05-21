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

import java.util.Collections;
import java.util.List;

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.endpoint.URIBuilder;

/**
 * @author David Dossot
 */
public class JcrMessageReceiverOverridesTestCase extends
		JcrMessageReceiverTestCase {

	private static final List UUID_LIST = Collections.singletonList("uuid_01");

	private static final List NODE_TYPE_NAME = Collections
			.singletonList("node_type_01");

	@Override
	public InboundEndpoint getEndpoint() throws Exception {
		final EndpointBuilder builder = new EndpointURIEndpointBuilder(
				new URIBuilder(
						"jcr://path/to/observedNode?contentPayloadType=full&eventTypes=5&deep=true&noLocal=false"),
				muleContext);

		builder.setConnector(JcrConnectorTestCase.newJcrConnector());

		endpoint = muleContext.getRegistry().lookupEndpointFactory()
				.getInboundEndpoint(builder);

		endpoint.getProperties().put("uuid", UUID_LIST);
		endpoint.getProperties().put("nodeTypeName", NODE_TYPE_NAME);

		return endpoint;
	}

	public void testReceiverProperties() throws Exception {
		final JcrMessageReceiver messageReceiver = (JcrMessageReceiver) getMessageReceiver();

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
