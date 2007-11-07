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

import java.util.Map;

import javax.jcr.Node;

import org.mule.impl.RequestContext;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.providers.NullPayload;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.UMOEvent;
import org.mule.umo.provider.UMOMessageDispatcher;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcherTestCase extends AbstractMuleTestCase {

	private JcrConnector connector;

	private UMOMessageDispatcher messageDispatcher;

	protected void doSetUp() throws Exception {
		super.doSetUp();

		connector = JcrConnectorTestCase.newJcrConnector();

		MuleEndpoint endpoint = new MuleEndpoint("jcr://"
				+ RepositoryTestSupport.ROOT_NODE_NAME, true);

		endpoint.setConnector(connector);

		messageDispatcher = new JcrMessageDispatcherFactory().create(endpoint);
	}

	protected void doTearDown() throws Exception {
		RequestContext.setEvent(null);

		connector.disconnect();
		connector.dispose();
		super.doTearDown();
	}

	public void testReceiveByUUID() throws Exception {
		Node targetNode = RepositoryTestSupport.getTestDataNode().addNode(
				"target");
		targetNode.addMixin("mix:referenceable");
		String uuid = targetNode.getUUID();
		RepositoryTestSupport.getSession().save();

		UMOEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, "foo");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageDispatcher.receive(0)
				.getPayload());

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
		RequestContext.setEvent(event);

		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);
	}

	public void testReceiveByEndpointUri() throws Exception {
		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);
	}
}
