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
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpointURI;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrEndpointTestCase extends AbstractMuleTestCase {

    public void testValidEndpointURI() throws Exception {
        UMOEndpointURI uri =
                new MuleEndpointURI("jcr://path/to/observedNode?eventTypes=5");

        assertEquals("jcr", uri.getScheme());
        assertEquals("/path/to/observedNode", uri.getAddress());

        assertEquals("path/to/observedNode", uri.getHost() + uri.getPath());
        assertEquals(1, uri.getParams().size());
        assertEquals("5", uri.getParams().getProperty("eventTypes"));
    }

    public void testValidRootEndpointURIWithParams() throws Exception {
        UMOEndpointURI uri = new MuleEndpointURI("jcr://?eventTypes=5");

        assertEquals("jcr", uri.getScheme());
        assertEquals("/", uri.getAddress());
        assertEquals(1, uri.getParams().size());
        assertEquals("5", uri.getParams().getProperty("eventTypes"));

        uri = new MuleEndpointURI("jcr:///?eventTypes=31");

        assertEquals("jcr", uri.getScheme());
        assertEquals("/", uri.getAddress());
        assertEquals(1, uri.getParams().size());
        assertEquals("31", uri.getParams().getProperty("eventTypes"));
    }

    public void testValidRootEndpointURI() throws Exception {
        UMOEndpointURI uri = new MuleEndpointURI("jcr:///");

        assertEquals("jcr", uri.getScheme());
        assertEquals("/", uri.getAddress());
    }

    public void testValidIndexedEndpointURI() throws Exception {
        UMOEndpointURI uri =
                JcrEndpointBuilder.newJcrEndpointURI("/indexed[1]/child[2]/bar");

        assertEquals("jcr", uri.getScheme());
        assertEquals("/indexed[1]/child[2]/bar", uri.getAddress());
    }

    public void testInvalidIndexedEndpointURI() throws Exception {
        try {
            new MuleEndpointURI("jcr:///indexed[1]/child[2]/bar");
            fail("should have got a MalformedEndpointException");
        } catch (MalformedEndpointException mee) {
            return;
        }
    }

}
