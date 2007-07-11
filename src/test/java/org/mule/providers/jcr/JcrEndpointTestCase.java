/*
 * \$Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr;

import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.endpoint.UMOEndpointURI;

public class JcrEndpointTestCase extends AbstractMuleTestCase
{

    /* For general guidelines on writing transports see
       http://mule.mulesource.org/display/MULE/Writing+Transports */

    public void testValidEndpointURI() throws Exception
    {
        UMOEndpointURI uri = new MuleEndpointURI("jcr://path/to/observedNode?eventTypes=5");

        assertEquals("jcr", uri.getScheme());
        assertEquals("jcr://path/to/observedNode?eventTypes=5", uri.getAddress());
        assertEquals("path/to/observedNode", uri.getHost() + uri.getPath());
        assertEquals(1, uri.getParams().size());
        assertEquals("5", uri.getParams().getProperty("eventTypes"));
    }

}
