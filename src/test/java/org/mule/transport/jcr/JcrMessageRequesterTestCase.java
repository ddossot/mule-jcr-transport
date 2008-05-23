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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.jcr.Node;

import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.routing.filter.Filter;
import org.mule.routing.filters.logic.AndFilter;
import org.mule.routing.filters.logic.NotFilter;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.NullPayload;
import org.mule.transport.jcr.filters.JcrNodeNameFilter;
import org.mule.transport.jcr.filters.JcrPropertyNameFilter;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageRequesterTestCase extends AbstractMuleTestCase {

	private JcrConnector connector;

	private JcrMessageRequester messageRequester;

	private String uuid;

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		newRequesterForTestEndpoint(null);

		// create some extra test nodes and properties
		RepositoryTestSupport.resetRepository();

		final Node testDataNode = RepositoryTestSupport.getTestDataNode();
		testDataNode.setProperty("pi", Math.PI);
		testDataNode.setProperty("stream", new ByteArrayInputStream("test"
				.getBytes()));
		testDataNode.setProperty("text", "EHLO SPAM");
		testDataNode.addMixin("mix:referenceable");
		uuid = testDataNode.getUUID();

		final Node target = testDataNode.addNode("noderelpath-target");
		target.setProperty("proprelpath-target", 123L);

		RepositoryTestSupport.getSession().save();
	}

	@Override
	protected void doTearDown() throws Exception {
		RequestContext.setEvent(null);

		connector.disconnect();
		connector.dispose();
		super.doTearDown();
	}

	private void newRequesterForTestEndpoint(final Filter filter)
			throws Exception {

		newRequesterForSpecificEndpoint("jcr://"
				+ RepositoryTestSupport.ROOT_NODE_NAME, filter);
	}

	private void newRequesterForSpecificEndpoint(final String uri,
			final Filter filter) throws Exception {

		final InboundEndpoint endpoint = JcrMessageReceiverTestCase
				.newInboundEndpoint(muleContext, uri, filter);

		connector = (JcrConnector) endpoint.getConnector();

		messageRequester = (JcrMessageRequester) new JcrMessageRequesterFactory()
				.create(endpoint);

	}

	public void testReceiveInputStreamNonStreamingEndpoint() throws Exception {
		final JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("stream");
		newRequesterForTestEndpoint(jcrPropertyNameFilter);

		final MuleMessage received = messageRequester.request(0);
		assertTrue(received.getPayload() instanceof InputStream);
	}

	public void testReceiveInputStreamStreamingEndpoint() throws Exception {
		final JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("stream");
		newRequesterForTestEndpoint(jcrPropertyNameFilter);

		final MuleMessage received = messageRequester.request(0);
		assertTrue(received.getPayload() instanceof InputStream);
	}

	// JcrConnector for Mule 2.x does not supporting "forced" streaming anymore.
	// Reactivate only if requested by end users.
	// public void testReceiveNonInputStreamStreamingEndpoint() throws Exception
	// {
	// final JcrPropertyNameFilter jcrPropertyNameFilter = new
	// JcrPropertyNameFilter();
	// jcrPropertyNameFilter.setPattern("text");
	// newRequesterForTestEndpoint(jcrPropertyNameFilter);
	//
	// final MuleMessage received = messageRequester.request(0);
	// assertTrue(received.getPayload() instanceof InputStream);
	// }

	public void testReceiveWithEventUUID() throws Exception {
		MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
		RequestContext.setEvent(event);

		assertTrue(messageRequester.request(0).getPayload() instanceof Map);

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, "pi");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveWithEventUUIDAndNodeRelpath() throws Exception {
		MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		RequestContext.setEvent(event);

		assertTrue(messageRequester.request(0).getPayload() instanceof Map);

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "bar");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveWithEventUUIDAndPropertyRelpath() throws Exception {
		MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "pi");
		RequestContext.setEvent(event);

		assertEquals(Double.class, messageRequester.request(0).getPayload()
				.getClass());

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "bar");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveWithEventQuery() throws Exception {
		MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_STATEMENT_PROPERTY,
				"//noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath");
		RequestContext.setEvent(event);

		assertTrue(messageRequester.request(0).getPayload() instanceof Map);

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_STATEMENT_PROPERTY, "/foo/bar");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveWithEventQueryAndNodeRelpath() throws Exception {
		MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_STATEMENT_PROPERTY,
				"//" + RepositoryTestSupport.ROOT_NODE_NAME);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		RequestContext.setEvent(event);

		assertTrue(messageRequester.request(0).getPayload() instanceof Map);

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_STATEMENT_PROPERTY,
				"//" + RepositoryTestSupport.ROOT_NODE_NAME);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "bar");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveWithEventQueryAndPropertyRelpath() throws Exception {
		MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_STATEMENT_PROPERTY,
				"//noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
				"proprelpath-target");
		RequestContext.setEvent(event);

		assertEquals(Long.class, messageRequester.request(0).getPayload()
				.getClass());

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_STATEMENT_PROPERTY,
				"//noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "bar");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveWithEventNodeRelpath() throws Exception {
		MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		RequestContext.setEvent(event);

		assertTrue(messageRequester.request(0).getPayload() instanceof Map);

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "bar");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveWithEventPropertyRelpath() throws Exception {
		MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "pi");
		RequestContext.setEvent(event);

		assertEquals(Double.class, messageRequester.request(0).getPayload()
				.getClass());

		event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "bar");
		RequestContext.setEvent(event);

		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveWithEventNodeAndPropertyRelpath() throws Exception {
		final MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
				"proprelpath-target");
		RequestContext.setEvent(event);

		assertEquals(Long.class, messageRequester.request(0).getPayload()
				.getClass());
	}

	public void testReceiveWithEventNodeAndPropertyRelpathFromRoot()
			throws Exception {

		newRequesterForSpecificEndpoint("jcr:///", null);

		final MuleEvent event = getTestEvent(null);

		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY,
				RepositoryTestSupport.ROOT_NODE_NAME + "/noderelpath-target");

		event.getMessage().setStringProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
				"proprelpath-target");

		RequestContext.setEvent(event);

		assertEquals(Long.class, messageRequester.request(0).getPayload()
				.getClass());
	}

	public void testReceiveByEndpointUriNoFilter() throws Exception {
		assertTrue(messageRequester.request(0).getPayload() instanceof Map);
	}

	public void testReceiveByEndpointUriWithBadFilter() throws Exception {

		try {
			newRequesterForTestEndpoint(new NotFilter());
		} catch (final IllegalArgumentException iae) {
			return;
		}

		fail("should have got an IAE");
	}

	public void testReceiveByEndpointUriWithPropertyNameFilter()
			throws Exception {
		JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("p*");
		newRequesterForTestEndpoint(jcrPropertyNameFilter);
		assertEquals(Double.class, messageRequester.request(0).getPayload()
				.getClass());

		jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("bar");
		newRequesterForTestEndpoint(jcrPropertyNameFilter);
		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveByEndpointUriWithNodeNameFilter() throws Exception {
		JcrNodeNameFilter jcrNodeNameFilter = new JcrNodeNameFilter();
		jcrNodeNameFilter.setPattern("*-target");
		newRequesterForTestEndpoint(jcrNodeNameFilter);
		assertFalse(NullPayload.getInstance().equals(
				messageRequester.request(0).getPayload()));

		jcrNodeNameFilter = new JcrNodeNameFilter();
		jcrNodeNameFilter.setPattern("noderelpath-*");
		newRequesterForTestEndpoint(jcrNodeNameFilter);
		assertTrue(messageRequester.request(0).getPayload() instanceof Map);

		jcrNodeNameFilter = new JcrNodeNameFilter();
		jcrNodeNameFilter.setPattern("bar");
		newRequesterForTestEndpoint(jcrNodeNameFilter);
		assertEquals(NullPayload.getInstance(), messageRequester.request(0)
				.getPayload());
	}

	public void testReceiveByEndpointUriWithBothNameFilters() throws Exception {
		final JcrNodeNameFilter jcrNodeNameFilter = new JcrNodeNameFilter();
		jcrNodeNameFilter.setPattern("noderelpath-*");

		final JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("proprelpath-**");

		final AndFilter andFilter = new AndFilter(jcrNodeNameFilter,
				jcrPropertyNameFilter);

		newRequesterForTestEndpoint(andFilter);

		assertEquals(Long.class, messageRequester.request(0).getPayload()
				.getClass());
	}

	public void testReceiveWithEventNodeRelpathAndPropertyFilter()
			throws Exception {

		final MuleEvent event = getTestEvent(null);
		event.getMessage().setStringProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
		RequestContext.setEvent(event);

		final JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
		jcrPropertyNameFilter.setPattern("proprelpath-**");
		newRequesterForTestEndpoint(jcrPropertyNameFilter);

		assertEquals(Long.class, messageRequester.request(0).getPayload()
				.getClass());
	}

}
