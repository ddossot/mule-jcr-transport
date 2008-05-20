/*
 * $Id: ConnectorTestCase.vm 10787 2008-02-12 18:51:50Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transport.Connector;
import org.mule.transport.AbstractConnectorTestCase;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrConnectorTestCase extends AbstractConnectorTestCase {
	@Override
	public Connector createConnector() throws Exception {
		return newJcrConnector();
	}

	static JcrConnector newJcrConnector() throws Exception,
			InitialisationException {

		final JcrConnector c = new JcrConnector();

		c.setName("Test-Jcr");
		c.setRepository(RepositoryTestSupport.getRepository());
		c.setUsername(RepositoryTestSupport.USERNAME);
		c.setPassword(RepositoryTestSupport.PASSWORD);
		c.setWorkspaceName(null);

		return c;
	}

	@Override
	public String getTestEndpointURI() {
		return "jcr://path/to/observedFolder";
	}

	@Override
	public Object getValidMessage() throws Exception {
		return "foo";
	}

	public void testInitializingWithoutConnector() {
		try {
			new JcrConnector().doInitialise();
			fail("An InitialisationException should have been thrown");
		} catch (final InitialisationException ie) {
			// expected
		}
	}

	public void testProperties() throws Exception {
		final JcrConnector jcrConnector = (JcrConnector) getConnector();

		assertEquals(RepositoryTestSupport.USERNAME, jcrConnector.getUsername());
		assertEquals(RepositoryTestSupport.PASSWORD, jcrConnector.getPassword());
		assertNull(jcrConnector.getWorkspaceName());
	}

}