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

		checkCoreProperties(c);

		assertNull(c.getUsername());
		assertNull(c.getPassword());
		assertNull(c.getWorkspaceName());
		assertEquals("NONE", c.getContentPayloadType());
		assertEquals(0, c.getEventTypes().intValue());
		assertTrue(c.isDeep());
		assertFalse(c.isNoLocal());

		assertNull(c.getNodeTypeNames());
		assertNull(c.getUuids());
	}

	public void testJcrConnectorFullConfiguration() throws Exception {
		final JcrConnector c = (JcrConnector) muleContext.getRegistry()
				.lookupConnector("jcrConnectorFullConfiguration");

		checkCoreProperties(c);

		assertEquals("admin", c.getUsername());
		assertEquals("admin", c.getPassword());
		assertEquals("test", c.getWorkspaceName());
		assertEquals("NOBINARY", c.getContentPayloadType());
		assertEquals(31, c.getEventTypes().intValue());
		assertFalse(c.isDeep());
		assertTrue(c.isNoLocal());

		assertEquals(Arrays.asList(new String[] { "oof", "rab" }), c
				.getNodeTypeNames());

		assertEquals(Arrays.asList(new String[] { "foo", "bar" }), c.getUuids());
	}

	private void checkCoreProperties(final JcrConnector c) {
		assertNotNull(c);
		assertTrue(c.isConnected());
		assertTrue(c.isStarted());
	}

}
