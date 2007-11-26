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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.mule.impl.RequestContext;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.providers.NullPayload;
import org.mule.providers.jcr.filters.JcrNodeNameFilter;
import org.mule.providers.jcr.filters.JcrPropertyNameFilter;
import org.mule.routing.filters.logic.AndFilter;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOFilter;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcherTestCase extends AbstractMuleTestCase {

	private JcrConnector connector;

	private JcrMessageDispatcher messageDispatcher;

	private MuleEndpoint endpoint;

	private String uuid;

	protected void doSetUp() throws Exception {
		super.doSetUp();

		connector = JcrConnectorTestCase.newJcrConnector();

		endpoint = new MuleEndpoint("jcr://"
				+ RepositoryTestSupport.ROOT_NODE_NAME, true);

		endpoint.setConnector(connector);

		messageDispatcher = (JcrMessageDispatcher) new JcrMessageDispatcherFactory()
				.create(endpoint);

		// create some extra test nodes and properties
		RepositoryTestSupport.resetRepository();

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
		setFilter(jcrPropertyNameFilter);

		assertTrue(messageDispatcher.receive(0).getPayload() instanceof InputStream);
	}

	public void testReceiveWithEventUUID() throws Exception {
		UMOEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
		RequestContext.setEvent(event);

		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, "foo");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageDispatcher.receive(0)
				.getPayload());
	}

	public void testReceiveWithEventNodeRelpath() throws Exception {
		UMOEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		RequestContext.setEvent(event);

		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "bar");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageDispatcher.receive(0)
				.getPayload());
	}

	public void testReceiveWithEventPropertyRelpath() throws Exception {
		UMOEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "foo");
		RequestContext.setEvent(event);

		assertEquals(Double.class, messageDispatcher.receive(0).getPayload()
				.getClass());

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "bar");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageDispatcher.receive(0)
				.getPayload());
	}

	public void testReceiveWithEventNodeAndPropertyRelpath() throws Exception {
		UMOEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
				"proprelpath-target");
		RequestContext.setEvent(event);

		assertEquals(Long.class, messageDispatcher.receive(0).getPayload()
				.getClass());
	}

	public void testReceiveByEndpointUriNoFilter() throws Exception {
		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);
	}

	public void testReceiveByEndpointUriWithPropertyNameFilter()
			throws Exception {
		JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("fo*");
		setFilter(jcrPropertyNameFilter);
		assertEquals(Double.class, messageDispatcher.receive(0).getPayload()
				.getClass());

		jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("bar");
		setFilter(jcrPropertyNameFilter);
		assertEquals(NullPayload.getInstance(), messageDispatcher.receive(0)
				.getPayload());
	}

	public void testReceiveByEndpointUriWithNodeNameFilter() throws Exception {
		JcrNodeNameFilter jcrNodeNameFilter = new JcrNodeNameFilter();
		jcrNodeNameFilter.setPattern("*-target");
		setFilter(jcrNodeNameFilter);
		assertFalse(NullPayload.getInstance().equals(
				messageDispatcher.receive(0).getPayload()));

		jcrNodeNameFilter = new JcrNodeNameFilter();
		jcrNodeNameFilter.setPattern("noderelpath-*");
		setFilter(jcrNodeNameFilter);
		assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);

		jcrNodeNameFilter = new JcrNodeNameFilter();
		jcrNodeNameFilter.setPattern("bar");
		setFilter(jcrNodeNameFilter);
		assertEquals(NullPayload.getInstance(), messageDispatcher.receive(0)
				.getPayload());
	}

	public void testReceiveByEndpointUriWithBothNameFilters() throws Exception {
		JcrNodeNameFilter jcrNodeNameFilter = new JcrNodeNameFilter();
		jcrNodeNameFilter.setPattern("noderelpath-*");

		JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("proprelpath-**");

		AndFilter andFilter = new AndFilter(jcrNodeNameFilter,
				jcrPropertyNameFilter);

		setFilter(andFilter);

		assertEquals(Long.class, messageDispatcher.receive(0).getPayload()
				.getClass());
	}

	public void testReceiveWithEventNodeRelpathAndPropertyFilter()
			throws Exception {

		UMOEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		RequestContext.setEvent(event);

		JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("proprelpath-**");
		setFilter(jcrPropertyNameFilter);

		assertEquals(Long.class, messageDispatcher.receive(0).getPayload()
				.getClass());
	}

	public void testStoreMapInNode() throws Exception {
		Map propertyNameAndValues = new HashMap();
		propertyNameAndValues.put("longProperty", new Long(1234));
		Calendar now = Calendar.getInstance();
		propertyNameAndValues.put("dateProperty", now);

		UMOEvent event = getTestEvent(propertyNameAndValues);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		RequestContext.setEvent(event);

		assertSame(propertyNameAndValues, messageDispatcher.doSend(event)
				.getPayload());

		Node node = RepositoryTestSupport.getTestDataNode().getNode(
				"noderelpath-target");

		assertEquals(1234L, node.getProperty("longProperty").getLong());
		assertEquals(now, RepositoryTestSupport.getTestDataNode().getNode(
				"noderelpath-target").getProperty("dateProperty").getDate());
	}

	public void testFailedStoreInNode() throws Exception {
		UMOEvent event = getTestEvent(new Object());
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");

		RequestContext.setEvent(event);

		try {
			messageDispatcher.doSend(event);
		} catch (IllegalArgumentException iae) {
			return;
		}

		fail("Should have got an IAE!");
	}

	public void testStoreCollectionInNode() throws Exception {
		Node node = RepositoryTestSupport.getTestDataNode().getNode(
				"noderelpath-target");

		node.setProperty("multiLongs", new String[] { "0" }, PropertyType.LONG);
		RepositoryTestSupport.getSession().save();

		List propertyValues = new ArrayList();
		propertyValues.add(new Long(1234));
		propertyValues.add(new Long(5678));

		UMOEvent event = getTestEvent(propertyValues);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "multiLongs");
		RequestContext.setEvent(event);

		assertSame(propertyValues, messageDispatcher.doSend(event).getPayload());
		assertEquals(2, node.getProperty("multiLongs").getValues().length);
	}

	public void testStoreSingleValueInNode() throws Exception {
		Long value = new Long(13579);
		UMOEvent event = getTestEvent(value);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
				"proprelpath-target");
		RequestContext.setEvent(event);

		assertSame(value, messageDispatcher.doSend(event).getPayload());
		assertEquals(value.longValue(), RepositoryTestSupport.getTestDataNode()
				.getNode("noderelpath-target")
				.getProperty("proprelpath-target").getLong());
	}

	public void testStoreInNewNode() throws Exception {
		UMOEvent event = getTestEvent("bar");

		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "new-node");

		event.getMessage().setStringProperty(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY,
				"nt:resource");

		event.getMessage().setStringProperty("jcr:mimeType", "text/plain");

		RequestContext.setEvent(event);

		assertNotNull(messageDispatcher.doSend(event).getPayload());

		assertEquals("nt:resource", RepositoryTestSupport.getTestDataNode()
				.getNode("new-node").getPrimaryNodeType().getName());
	}

	public void testStoreInForcedNewNode() throws Exception {
		UMOEvent event = getTestEvent("bar");

		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "new-forced-node");

		event.getMessage().setStringProperty(
				JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY, "true");

		RequestContext.setEvent(event);

		assertNotNull(messageDispatcher.doSend(event).getPayload());
		assertNotNull(messageDispatcher.doSend(event).getPayload());

		assertEquals(2, RepositoryTestSupport.getTestDataNode().getNodes(
				"new-forced-node").getSize());
	}

	public void testFailedStoreUnderProperty() throws Exception {
		endpoint = new MuleEndpoint("jcr://"
				+ RepositoryTestSupport.ROOT_NODE_NAME + "/foo", true);

		endpoint.setConnector(connector);

		messageDispatcher = (JcrMessageDispatcher) new JcrMessageDispatcherFactory()
				.create(endpoint);

		UMOEvent event = getTestEvent("bar");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY, "true");
		RequestContext.setEvent(event);

		try {
			messageDispatcher.doSend(event).getPayload();
		} catch (IllegalArgumentException iae) {
			return;
		}

		fail("Should have got an IAE");
	}

	private void setFilter(UMOFilter filter) {
		endpoint.setFilter(filter);
		messageDispatcher.refreshEndpointFilter();
	}
}
