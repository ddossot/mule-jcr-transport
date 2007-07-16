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

import org.mule.tck.providers.AbstractConnectorTestCase;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrConnectorTestCase extends AbstractConnectorTestCase {
	public UMOConnector getConnector() throws Exception {
		JcrConnector c = new JcrConnector();
		c.setName("Test-Jcr");
		c.setRepository(RepositoryTestSupport.getRepository());
		c.setUsername(RepositoryTestSupport.USERNAME);
		c.setPassword(RepositoryTestSupport.PASSWORD);
		c.setWorkspaceName(null);
		c.initialise();
		c.connect();
		return c;
	}

	public String getTestEndpointURI() {
		return "jcr://path/to/observedFolder";
	}

	public Object getValidMessage() throws Exception {
		return JcrMessageAdapterTestCase.getJcrEvents();
	}

	public void testInitializingWithoutConnector() {
		try {
			new JcrConnector().doInitialise();
			fail("An InitialisationException should have been thrown");
		} catch (InitialisationException ie) {
			// expected
		}
	}

	public void testProperties() throws Exception {
		JcrConnector jcrConnector = (JcrConnector) connector;

		assertNotNull(jcrConnector.getSession());
		assertEquals(RepositoryTestSupport.USERNAME, jcrConnector.getUsername());
		assertEquals(RepositoryTestSupport.PASSWORD, jcrConnector.getPassword());
		assertNull(jcrConnector.getWorkspaceName());
	}

	public void testConnectorMessageDispatcherFactory() throws Exception {
		// disables this test as the connector does not have a dispatcher
	}

}
