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

import javax.jcr.Repository;

import org.apache.jackrabbit.core.TransientRepository;
import org.mule.tck.providers.AbstractConnectorTestCase;
import org.mule.umo.provider.UMOConnector;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrConnectorTestCase extends AbstractConnectorTestCase {
	private Repository repository;

	public JcrConnectorTestCase() throws Exception {
		repository = new TransientRepository();
	}

	public UMOConnector getConnector() throws Exception {
		JcrConnector c = new JcrConnector();
		c.setName("Test-Jcr");
		c.setRepository(repository);
		c.initialise();
		return c;
	}

	public String getTestEndpointURI() {
		return "jcr://path/to/observedFolder";
	}

	public Object getValidMessage() throws Exception {
		return JcrMessageAdapterTestCase.getJcrEvents();
	}

	public void testProperties() throws Exception {
		// TODO test setting and retrieving any custom properties on the
		// Connector as necessary
	}

	public void testConnectorMessageDispatcherFactory() throws Exception {
		// disables this test as the connector does not have a dispatcher
	}

}
