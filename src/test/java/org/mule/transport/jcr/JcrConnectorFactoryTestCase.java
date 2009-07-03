/*
 * $Id: ConnectorFactoryTestCase.vm 11571 2008-04-12 00:22:07Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import org.mule.api.transport.Connector;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.service.TransportFactory;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrConnectorFactoryTestCase extends AbstractMuleTestCase {

    public void testCreateFromFactory() throws Exception {
        final Connector connector =
                new TransportFactory(muleContext).createConnector(new MuleEndpointURI(getEndpointURI(), muleContext),
                        muleContext);

        assertNotNull(connector);
        assertTrue(connector instanceof JcrConnector);
    }

    public String getEndpointURI() {
        return "jcr://path/to/observedNode";
    }

}
