/*
 * \$Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Test;
import org.mule.api.MuleContext;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.MalformedEndpointException;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.routing.filter.Filter;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.endpoint.URIBuilder;
import org.mule.routing.MessageFilter;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrEndpointTestCase extends AbstractMuleContextTestCase
{
    @Test
    public void testValidEndpointURI() throws Exception
    {
        final EndpointURI uri = new MuleEndpointURI("jcr://path/to/observedNode?eventTypes=5", muleContext);

        uri.initialise();

        assertEquals("jcr", uri.getScheme());
        assertEquals("/path/to/observedNode", uri.getAddress());

        assertEquals("path/to/observedNode", uri.getHost() + uri.getPath());
        assertEquals(1, uri.getParams().size());
        assertEquals("5", uri.getParams().getProperty("eventTypes"));
    }

    @Test
    public void testValidRootEndpointURIWithParams() throws Exception
    {
        EndpointURI uri = new MuleEndpointURI("jcr://?eventTypes=5", muleContext);
        uri.initialise();

        assertEquals("jcr", uri.getScheme());
        assertEquals("/", uri.getAddress());
        assertEquals(1, uri.getParams().size());
        assertEquals("5", uri.getParams().getProperty("eventTypes"));

        uri = new MuleEndpointURI("jcr:///?eventTypes=31", muleContext);
        uri.initialise();

        assertEquals("jcr", uri.getScheme());
        assertEquals("/", uri.getAddress());
        assertEquals(1, uri.getParams().size());
        assertEquals("31", uri.getParams().getProperty("eventTypes"));
    }

    @Test
    public void testValidRootEndpointURI() throws Exception
    {
        final EndpointURI uri = new MuleEndpointURI("jcr:///", muleContext);
        uri.initialise();

        assertEquals("jcr", uri.getScheme());
        assertEquals("/", uri.getAddress());
    }

    @Test
    public void testValidIndexedEndpointURI() throws Exception
    {
        final EndpointURI uri = JcrEndpointURIBuilder.newJcrEndpointURI("/indexed[1]/child[2]/bar",
            muleContext);

        uri.initialise();

        assertEquals("jcr", uri.getScheme());
        assertEquals("/indexed[1]/child[2]/bar", uri.getAddress());
    }

    @Test
    public void testInvalidIndexedEndpointURI() throws Exception
    {
        try
        {
            new MuleEndpointURI("jcr:///indexed[1]/child[2]/bar", muleContext).initialise();
            fail("should have got a MalformedEndpointException");
        }
        catch (final MalformedEndpointException mee)
        {
            return;
        }
    }

    static InboundEndpoint newInboundEndpoint(final MuleContext muleContext, final String address)
        throws Exception
    {
        return newInboundEndpoint(muleContext, address, null);
    }

    static InboundEndpoint newInboundEndpoint(final MuleContext muleContext,
                                              final String address,
                                              final Filter filter) throws Exception
    {

        final EndpointBuilder builder = newEndpointBuilder(muleContext, address, filter);

        return muleContext.getEndpointFactory().getInboundEndpoint(builder);
    }

    static OutboundEndpoint newOutboundEndpoint(final MuleContext muleContext,
                                                final String address,
                                                final Filter filter) throws Exception
    {

        final EndpointBuilder builder = newEndpointBuilder(muleContext, address, filter);

        return muleContext.getEndpointFactory().getOutboundEndpoint(builder);
    }

    private static EndpointBuilder newEndpointBuilder(final MuleContext muleContext,
                                                      final String address,
                                                      final Filter filter)
        throws Exception, InitialisationException
    {

        final EndpointBuilder builder = new EndpointURIEndpointBuilder(new URIBuilder(address, muleContext));

        if (filter != null)
        {
            builder.setMessageProcessors(Collections.singletonList((MessageProcessor) new MessageFilter(
                filter)));
        }

        final JcrConnector jcrConnector = JcrConnectorTestCase.newJcrConnector(muleContext);
        jcrConnector.initialise();
        builder.setConnector(jcrConnector);
        return builder;
    }
}
