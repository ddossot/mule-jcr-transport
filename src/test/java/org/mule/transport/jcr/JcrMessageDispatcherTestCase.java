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

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.mule.DefaultMuleMessage;
import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.DispatchException;
import org.mule.transport.jcr.handlers.NodeTypeHandler;
import org.mule.transport.jcr.handlers.NodeTypeHandlerManager;
import org.mule.util.IOUtils;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcherTestCase extends AbstractJcrMessagerTestCase {

	public static final class NtQueryNodeTypeHandler implements NodeTypeHandler {

		public String getNodeTypeName() {
			return "nt:query";
		}

		public Node createNode(final Session session, final Node targetNode,
				final String nodeRelPath, final MuleMessage message)
				throws RepositoryException, IOException {

			final Node node = targetNode
					.addNode(nodeRelPath, getNodeTypeName());
			updateContent(session, node, message);
			return node;
		}

		public void updateContent(final Session session, final Node node,
				final MuleMessage message) throws RepositoryException,
				IOException {
			node.setProperty("jcr:statement", message.getStringProperty(
					"jcr:statement", null));
			node.setProperty("jcr:language", message.getStringProperty(
					"jcr:language", null));
		}

		public void initialize(final NodeTypeHandlerManager manager) {
			// ignore the manager
		}
	}

	private JcrMessageDispatcher messageDispatcher;

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		newDispatcherForTestEndpoint("jcr://"
				+ RepositoryTestSupport.ROOT_NODE_NAME);
	}

	private void newDispatcherForTestEndpoint(final String uri)
			throws Exception {

		final OutboundEndpoint endpoint = JcrEndpointTestCase
				.newOutboundEndpoint(muleContext, uri, null);

		connector = (JcrConnector) endpoint.getConnector();

		messageDispatcher = (JcrMessageDispatcher) new JcrMessageDispatcherFactory()
				.create(endpoint);

	}

	public void testStoreMapInNode() throws Exception {
		final Map propertyNameAndValues = new HashMap();
		propertyNameAndValues.put("longProperty", new Long(1234));
		final Calendar now = Calendar.getInstance();
		propertyNameAndValues.put("dateProperty", now);

		final MuleEvent event = getTestEvent(propertyNameAndValues);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		RequestContext.setEvent(event);

		assertSame(propertyNameAndValues, messageDispatcher.doSend(event)
				.getPayload());

		final Node node = RepositoryTestSupport.getTestDataNode().getNode(
				"noderelpath-target");

		assertEquals(1234L, node.getProperty("longProperty").getLong());
		assertEquals(now, RepositoryTestSupport.getTestDataNode().getNode(
				"noderelpath-target").getProperty("dateProperty").getDate());
	}

	public void testFailedStoreInNode() throws Exception {
		final MuleEvent event = getTestEvent(new Object());
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");

		RequestContext.setEvent(event);

		try {
			messageDispatcher.doSend(event);
		} catch (final IllegalArgumentException iae) {
			return;
		}

		fail("Should have got an IllegalArgumentException!");
	}

	public void testFailedStoreInNodeFromUUID() throws Exception {
		final MuleEvent event = getTestEvent(new Object());
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, "foo-bar-uuid");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
				"proprelpath-target");

		RequestContext.setEvent(event);

		try {
			messageDispatcher.doSend(event);
		} catch (final DispatchException de) {
			return;
		}

		fail("Should have got a DispatchException!");
	}

	public void testFailedStoreInNodeFromQuery() throws Exception {
		final MuleEvent event = getTestEvent(new Object());
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_STATEMENT_PROPERTY, "/foo/bar");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath");

		RequestContext.setEvent(event);

		try {
			messageDispatcher.doSend(event);
		} catch (final DispatchException de) {
			return;
		}

		fail("Should have got a DispatchException!");
	}

	public void testStoreCollectionInNode() throws Exception {
		final Node node = RepositoryTestSupport.getTestDataNode().getNode(
				"noderelpath-target");

		node.setProperty("multiLongs", new String[] { "0" }, PropertyType.LONG);
		RepositoryTestSupport.getSession().save();

		final List propertyValues = new ArrayList();
		propertyValues.add(new Long(1234));
		propertyValues.add(new Long(5678));

		final MuleEvent event = getTestEvent(propertyValues);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "multiLongs");
		RequestContext.setEvent(event);

		assertSame(propertyValues, messageDispatcher.doSend(event).getPayload());
		assertEquals(2, node.getProperty("multiLongs").getValues().length);
	}

	public void testStoreSingleValueInNode() throws Exception {
		final Long value = new Long(13579);
		final MuleEvent event = getTestEvent(value);
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

	public void testStoreSingleValueInNodeFromUUID() throws Exception {
		final Long value = new Long(4862);
		final MuleEvent event = getTestEvent(value);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
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

	public void testStoreSingleValueInNodeFromQuery() throws Exception {
		final Long value = new Long(8246);
		final MuleEvent event = getTestEvent(value);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_STATEMENT_PROPERTY,
				"//noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath");
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
		final MuleEvent event = getTestEvent("bar");

		final String newNodeName = "new-node";

		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, newNodeName);

		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:resource");

		event.getMessage().setStringProperty("jcr:mimeType", "text/plain");

		RequestContext.setEvent(event);

		assertNotNull(messageDispatcher.doSend(event).getPayload());

		assertEquals("nt:resource", RepositoryTestSupport.getTestDataNode()
				.getNode(newNodeName).getPrimaryNodeType().getName());
	}

	public void testStoreInNewNodeWithDispatch() throws Exception {
		final MuleEvent event = getTestEvent("bar");

		final String newNodeName = "new-node-dispatched";

		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, newNodeName);

		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:resource");

		event.getMessage().setStringProperty("jcr:mimeType", "text/plain");

		RequestContext.setEvent(event);

		messageDispatcher.doDispatch(event);

		assertEquals("nt:resource", RepositoryTestSupport.getTestDataNode()
				.getNode(newNodeName).getPrimaryNodeType().getName());
	}

	public void testStoreCustomTypeHandler() throws Exception {
		connector.setCustomNodeTypeHandlers(Collections
				.singletonList(NtQueryNodeTypeHandler.class.getName()));

		final MuleEvent event = getTestEvent(null);

		final String newNodeName = "nt-query-node";

		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, newNodeName);

		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:query");

		event.getMessage().setStringProperty("jcr:statement", "/foo/bar");
		event.getMessage().setStringProperty("jcr:language", Query.XPATH);

		RequestContext.setEvent(event);

		assertNotNull(messageDispatcher.doSend(event).getPayload());

		final Node node = RepositoryTestSupport.getTestDataNode().getNode(
				newNodeName);

		assertEquals("nt:query", node.getPrimaryNodeType().getName());
		assertEquals(Query.XPATH, node.getProperty("jcr:language").getString());
		assertEquals("/foo/bar", node.getProperty("jcr:statement").getString());
	}

	public void testStoreInForcedNewNode() throws Exception {
		final MuleEvent event = getTestEvent("bar");

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
		newDispatcherForTestEndpoint("jcr://"
				+ RepositoryTestSupport.ROOT_NODE_NAME + "/pi");

		final MuleEvent event = getTestEvent("bar");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY, "true");
		RequestContext.setEvent(event);

		try {
			messageDispatcher.doSend(event).getPayload();
		} catch (final IllegalArgumentException iae) {
			return;
		}

		fail("Should have got an IAE");
	}

	public void testConnectorGetOutputStream() throws Exception {
		final Map properties = new HashMap();
		properties.put(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "stored-stream");
		properties.put(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:resource");
		properties.put("jcr:mimeType", "text/plain");

		final MuleMessage message = new DefaultMuleMessage(null, properties);

		final OutputStream outputStream = connector.getOutputStream(
				(OutboundEndpoint) messageDispatcher.getEndpoint(), message);

		assertNotNull(outputStream);

		final String testContent = "test streamed content";
		IOUtils.copy(new StringReader(testContent), outputStream);

		outputStream.flush();
		outputStream.close();

		final Property property = RepositoryTestSupport.getTestDataNode()
				.getNode("stored-stream").getProperty("jcr:data");

		assertNotNull(property);

		final StringWriter sw = new StringWriter();
		IOUtils.copy(property.getStream(), sw);

		assertEquals(testContent, sw.toString());
	}

}
