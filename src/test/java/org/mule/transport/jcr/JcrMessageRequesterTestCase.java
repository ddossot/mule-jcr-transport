/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Map;

import org.junit.Test;
import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.routing.filter.Filter;
import org.mule.api.transport.PropertyScope;
import org.mule.routing.filters.logic.AndFilter;
import org.mule.routing.filters.logic.NotFilter;
import org.mule.transport.NullPayload;
import org.mule.transport.jcr.filters.JcrNodeNameFilter;
import org.mule.transport.jcr.filters.JcrPropertyNameFilter;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageRequesterTestCase extends AbstractJcrMessagerTestCase
{
    private JcrMessageRequester messageRequester;

    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        newRequesterForTestEndpoint(null);
    }

    private void newRequesterForTestEndpoint(final Filter filter) throws Exception
    {

        newRequesterForSpecificEndpoint("jcr://" + RepositoryTestSupport.ROOT_NODE_NAME, filter);
    }

    private void newRequesterForSpecificEndpoint(final String uri, final Filter filter) throws Exception
    {
        final InboundEndpoint endpoint = JcrEndpointTestCase.newInboundEndpoint(muleContext, uri, filter);

        connector = (JcrConnector) endpoint.getConnector();
        connector.start();
        connector.connect();

        messageRequester = (JcrMessageRequester) new JcrMessageRequesterFactory().create(endpoint);
        messageRequester.initialise();
    }

    @Test
    public void testReceiveInputStreamNonStreamingEndpoint() throws Exception
    {
        final JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("stream");
        newRequesterForTestEndpoint(jcrPropertyNameFilter);

        final MuleMessage received = messageRequester.request(0);
        assertTrue(received.getPayload() instanceof InputStream);
    }

    @Test
    public void testReceiveInputStreamStreamingEndpoint() throws Exception
    {
        final JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("stream");
        newRequesterForTestEndpoint(jcrPropertyNameFilter);

        final MuleMessage received = messageRequester.request(0);
        assertTrue(received.getPayload() instanceof InputStream);
    }

    @Test
    public void testReceiveWithEventUUID() throws Exception
    {
        MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_UUID_PROPERTY, uuid, PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertTrue(messageRequester.request(0).getPayload() instanceof Map<?, ?>);

        event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_UUID_PROPERTY, "pi", PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveWithEventUUIDAndNodeRelpath() throws Exception
    {
        MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_UUID_PROPERTY, uuid, PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertTrue(messageRequester.request(0).getPayload() instanceof Map<?, ?>);

        event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_UUID_PROPERTY, uuid, PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "bar",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveWithEventUUIDAndPropertyRelpath() throws Exception
    {
        MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_UUID_PROPERTY, uuid, PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "pi",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(Double.class, messageRequester.request(0).getPayload().getClass());

        event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_UUID_PROPERTY, uuid, PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "bar",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveWithEventQuery() throws Exception
    {
        MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY, "//noderelpath-target",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertTrue(messageRequester.request(0).getPayload() instanceof Map<?, ?>);

        event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY, "/foo/bar",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveWithEventQueryAndNodeRelpath() throws Exception
    {
        MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY,
            "//" + RepositoryTestSupport.ROOT_NODE_NAME, PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertTrue(messageRequester.request(0).getPayload() instanceof Map<?, ?>);

        event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY,
            "//" + RepositoryTestSupport.ROOT_NODE_NAME, PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "bar",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveWithEventQueryAndPropertyRelpath() throws Exception
    {
        MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY, "//noderelpath-target",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "proprelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(Long.class, messageRequester.request(0).getPayload().getClass());

        event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY, "//noderelpath-target",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "bar",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveWithEventNodeRelpath() throws Exception
    {
        MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertTrue(messageRequester.request(0).getPayload() instanceof Map<?, ?>);

        event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "bar",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveWithEventPropertyRelpath() throws Exception
    {
        MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "pi",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(Double.class, messageRequester.request(0).getPayload().getClass());

        event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "bar",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveWithEventNodeAndPropertyRelpath() throws Exception
    {
        final MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "proprelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertEquals(Long.class, messageRequester.request(0).getPayload().getClass());
    }

    @Test
    public void testReceiveWithEventNodeAndPropertyRelpathFromRoot() throws Exception
    {

        newRequesterForSpecificEndpoint("jcr:///", null);

        final MuleEvent event = getTestEvent(null);

        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY,
            RepositoryTestSupport.ROOT_NODE_NAME + "/noderelpath-target", PropertyScope.INVOCATION);

        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "proprelpath-target",
            PropertyScope.INVOCATION);

        RequestContext.setEvent(event);

        assertEquals(Long.class, messageRequester.request(0).getPayload().getClass());
    }

    @Test
    public void testReceiveByEndpointUriNoFilter() throws Exception
    {
        assertTrue(messageRequester.request(0).getPayload() instanceof Map<?, ?>);
    }

    @Test
    public void testReceiveByEndpointUriWithBadFilter() throws Exception
    {

        try
        {
            newRequesterForTestEndpoint(new NotFilter());
        }
        catch (final IllegalArgumentException iae)
        {
            return;
        }

        fail("should have got an IAE");
    }

    @Test
    public void testReceiveByEndpointUriWithPropertyNameFilter() throws Exception
    {
        JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("p*");
        newRequesterForTestEndpoint(jcrPropertyNameFilter);
        assertEquals(Double.class, messageRequester.request(0).getPayload().getClass());

        jcrPropertyNameFilter = new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("bar");
        newRequesterForTestEndpoint(jcrPropertyNameFilter);
        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveByEndpointUriWithNodeNameFilter() throws Exception
    {
        JcrNodeNameFilter jcrNodeNameFilter = new JcrNodeNameFilter();
        jcrNodeNameFilter.setPattern("*-target");
        newRequesterForTestEndpoint(jcrNodeNameFilter);
        assertFalse(NullPayload.getInstance().equals(messageRequester.request(0).getPayload()));

        jcrNodeNameFilter = new JcrNodeNameFilter();
        jcrNodeNameFilter.setPattern("noderelpath-*");
        newRequesterForTestEndpoint(jcrNodeNameFilter);
        assertTrue(messageRequester.request(0).getPayload() instanceof Map<?, ?>);

        jcrNodeNameFilter = new JcrNodeNameFilter();
        jcrNodeNameFilter.setPattern("bar");
        newRequesterForTestEndpoint(jcrNodeNameFilter);
        assertEquals(NullPayload.getInstance(), messageRequester.request(0).getPayload());
    }

    @Test
    public void testReceiveByEndpointUriWithBothNameFilters() throws Exception
    {
        final JcrNodeNameFilter jcrNodeNameFilter = new JcrNodeNameFilter();
        jcrNodeNameFilter.setPattern("noderelpath-*");

        final JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("proprelpath-**");

        final AndFilter andFilter = new AndFilter(jcrNodeNameFilter, jcrPropertyNameFilter);

        newRequesterForTestEndpoint(andFilter);

        assertEquals(Long.class, messageRequester.request(0).getPayload().getClass());
    }

    @Test
    public void testReceiveWithEventNodeRelpathAndPropertyFilter() throws Exception
    {

        final MuleEvent event = getTestEvent(null);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        final JcrPropertyNameFilter jcrPropertyNameFilter = new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("proprelpath-**");
        newRequesterForTestEndpoint(jcrPropertyNameFilter);

        assertEquals(Long.class, messageRequester.request(0).getPayload().getClass());
    }
}
