/*
 * \$Id: EndpointTestCase.vm 11571 2008-04-12 00:22:07Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import org.mule.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.api.endpoint.EndpointURI;


public class JcrEndpointTestCase extends AbstractMuleTestCase
{

    /* For general guidelines on writing transports see
       http://mule.mulesource.org/display/MULE/Writing+Transports */

    public void testValidEndpointURI() throws Exception
    {
        // TODO test creating and asserting Endpoint values eg

        /*
        EndpointURI url = new MuleEndpointURI("tcp://localhost:7856");
        assertEquals("tcp", url.getScheme());
        assertEquals("tcp://localhost:7856", url.getAddress());
        assertNull(url.getEndpointName());
        assertEquals(7856, url.getPort());
        assertEquals("localhost", url.getHost());
        assertEquals("tcp://localhost:7856", url.getAddress());
        assertEquals(0, url.getParams().size());
        */

        throw new UnsupportedOperationException("testValidEndpointURI");
    }

}
