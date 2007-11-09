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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.jcr.Node;

import org.mule.impl.RequestContext;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.providers.NullPayload;
import org.mule.providers.jcr.filters.JcrPropertyNameFilter;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.UMOEvent;
import org.mule.umo.provider.UMOMessageDispatcher;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcherTestCase extends AbstractMuleTestCase {

	private JcrConnector connector;

	private UMOMessageDispatcher messageDispatcher;

	private MuleEndpoint endpoint;

	private String uuid;

	protected void doSetUp() throws Exception {
		super.doSetUp();

		connector = JcrConnectorTestCase.newJcrConnector();

		endpoint = new MuleEndpoint("jcr://"
				+ RepositoryTestSupport.ROOT_NODE_NAME, true);

		endpoint.setConnector(connector);

		messageDispatcher = new JcrMessageDispatcherFactory().create(endpoint);

		// create some extra test nodes and properties
		Node testDataNode = RepositoryTestSupport.getTestDataNode();
		testDataNode.setProperty("foo", Math.PI);
		testDataNode.setProperty("stream", new ByteArrayInputStream("test"
				.getBytes()));

		Node target = testDataNode.addNode("noderelpath-target");
		target.setProperty("proprelpath-target", 123L);

		target = testDataNode.addNode("uuid-target");
		target.addMixin("mix:referenceable");
		uuid = target.getUUID();

		RepositoryTestSupport.getSession().save();
	}

	protected void doTearDown() throws Exception {
		RequestContext.setEvent(null);

		connector.disconnect();
		connector.dispose();
		super.doTearDown();
	}

	public void testReceiveInputStream() throws Exception {
		JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("stream");
		endpoint.setFilter(jcrPropertyNameFilter);

		assertTrue(messageDispatcher.receive(0).getPayload() instanceof InputStream);
	}

	public void testReceiveWithEventUUID() throws Exception {
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

	public void testReceiveWithEventNodeRelpath() throws Exception {
		UMOEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		RequestContext.setEvent(event);

		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);
	}

	public void testReceiveWithEventPropertyRelpath() throws Exception {
		UMOEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "foo");
		RequestContext.setEvent(event);

		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Double);
	}

	public void testReceiveWithEventNodeAndPropertyRelpath() throws Exception {
		UMOEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
				"proprelpath-target");
		RequestContext.setEvent(event);

		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Long);
	}

	public void testReceiveByEndpointUriNoFilter() throws Exception {
		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);
	}

	public void testReceiveByEndpointUriWithFilter() throws Exception {
		JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("foo");
		endpoint.setFilter(jcrPropertyNameFilter);
		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Double);

		jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("bar");
		endpoint.setFilter(jcrPropertyNameFilter);
		assertEquals(NullPayload.getInstance(), messageDispatcher.receive(0)
				.getPayload());
	}

	// TODO test noderelpath and proprelpath pointing missing items

	// TODO test noderelpath + filter and proprelpath + filter (the filter
	// should not play in the latter)
}
