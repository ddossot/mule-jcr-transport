/*
 * $Id: NamespaceHandlerTestCase.vm 10787 2008-02-12 18:51:50Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.jcr;

import java.util.Arrays;
import java.util.Map;

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.tck.FunctionalTestCase;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrNamespaceHandlerTestCase extends FunctionalTestCase {
	@Override
	protected String getConfigResources() {
		return "jcr-namespace-config.xml";
	}

	public void testJcrConnectorMinimumConfiguration() throws Exception {
		final JcrConnector c = (JcrConnector) muleContext.getRegistry()
				.lookupConnector("jcrConnectorMinimumConfiguration");

		checkCoreConnectorProperties(c);

		assertNull(c.getUsername());
		assertNull(c.getPassword());
		assertNull(c.getWorkspaceName());
		assertEquals("NONE", c.getContentPayloadType());
		assertEquals(0, c.getEventTypes().intValue());
		assertTrue(c.isDeep());
		assertFalse(c.isNoLocal());

		assertNull(c.getUuids());
		assertNull(c.getNodeTypeNames());
	}

	public void testJcrConnectorFullConfiguration() throws Exception {
		final JcrConnector c = (JcrConnector) muleContext.getRegistry()
				.lookupConnector("jcrConnectorFullConfiguration");

		checkCoreConnectorProperties(c);

		assertEquals("admin", c.getUsername());
		assertEquals("admin", c.getPassword());
		assertEquals("test", c.getWorkspaceName());
		assertEquals("NOBINARY", c.getContentPayloadType());
		assertEquals(31, c.getEventTypes().intValue());
		assertFalse(c.isDeep());
		assertTrue(c.isNoLocal());

		assertEquals(Arrays.asList(new String[] { "foo", "bar" }), c.getUuids());

		assertEquals(Arrays.asList(new String[] { "oof", "rab" }), c
				.getNodeTypeNames());
	}

	private void checkCoreConnectorProperties(final JcrConnector c) {
		assertNotNull(c);
		assertTrue(c.isConnected());
		assertTrue(c.isStarted());
	}

	public void testGlobalJcrEndpointMinimumConfiguration() throws Exception {
		final EndpointBuilder endpointBuilder = muleContext.getRegistry()
				.lookupEndpointFactory().getEndpointBuilder(
						"jcrEndpointMinimumConfiguration");

		assertNotNull(endpointBuilder);

		final InboundEndpoint inboundEndpoint = endpointBuilder
				.buildInboundEndpoint();

		assertNotNull(inboundEndpoint);
		assertEquals("/min", inboundEndpoint.getEndpointURI().getAddress());

		final Map props = inboundEndpoint.getProperties();
		// TODO factor out and call other tests from next one
		assertNull(JcrConnector.getTokenizedValues((String) props
				.get(JcrConnector.JCR_NODE_TYPE_NAME_LIST_PROPERTY)));

		assertNull(JcrConnector.getTokenizedValues((String) props
				.get(JcrConnector.JCR_UUID_LIST_PROPERTY)));
	}

	public void testGlobalJcrEndpointFullConfiguration() throws Exception {
		final EndpointBuilder endpointBuilder = muleContext.getRegistry()
				.lookupEndpointFactory().getEndpointBuilder(
						"jcrEndpointFullConfiguration");

		assertNotNull(endpointBuilder);

		final InboundEndpoint inboundEndpoint = endpointBuilder
				.buildInboundEndpoint();

		assertNotNull(inboundEndpoint);
		assertEquals("/full", inboundEndpoint.getEndpointURI().getAddress());

		final Map props = inboundEndpoint.getProperties();
		assertEquals("true", props.get(JcrConnector.JCR_NO_LOCAL_PROPERTY));
		assertEquals("xpath", props
				.get(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY));

		assertEquals("/query", props
				.get(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY));

		assertEquals("true", props
				.get(JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY));

		assertEquals("child", props.get(JcrConnector.JCR_NODE_RELPATH_PROPERTY));

		assertEquals("prop", props
				.get(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY));

		assertEquals("false", props.get(JcrConnector.JCR_DEEP_PROPERTY));
		assertEquals("FULL", props
				.get(JcrConnector.JCR_CONTENT_PAYLOAD_TYPE_PROPERTY));

		assertEquals("4", props.get(JcrConnector.JCR_EVENT_TYPES_PROPERTY));

		assertEquals("name", props
				.get(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY));

		assertEquals("u-u-i-d", props.get(JcrConnector.JCR_NODE_UUID_PROPERTY));

		assertEquals(Arrays.asList(new String[] { "oof", "rab" }), JcrConnector
				.getTokenizedValues((String) props
						.get(JcrConnector.JCR_NODE_TYPE_NAME_LIST_PROPERTY)));

		assertEquals(Arrays.asList(new String[] { "foo", "bar" }), JcrConnector
				.getTokenizedValues((String) props
						.get(JcrConnector.JCR_UUID_LIST_PROPERTY)));
	}
	// TODO test in and out endpoints in a service
}
