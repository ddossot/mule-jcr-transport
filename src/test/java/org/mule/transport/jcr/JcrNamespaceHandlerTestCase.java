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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.routing.filter.Filter;
import org.mule.api.service.Service;
import org.mule.api.transformer.Transformer;
import org.mule.routing.filters.logic.AndFilter;
import org.mule.routing.outbound.DefaultOutboundRouterCollection;
import org.mule.routing.outbound.OutboundPassThroughRouter;
import org.mule.service.ServiceCompositeMessageSource;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.jcr.config.JcrNamespaceHandler;
import org.mule.transport.jcr.filters.JcrNodeNameFilter;
import org.mule.transport.jcr.filters.JcrPropertyNameFilter;
import org.mule.transport.jcr.handlers.NtQueryNodeTypeHandler;
import org.mule.transport.jcr.transformers.JcrEventToObject;
import org.mule.transport.jcr.transformers.JcrItemToObject;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrNamespaceHandlerTestCase extends FunctionalTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "jcr-namespace-config.xml";
    }

    @Test
    public void testJcrConnectorMinimumConfiguration() throws Exception
    {
        final JcrConnector c = (JcrConnector) muleContext.getRegistry().lookupConnector(
            "jcrConnectorMinimumConfiguration");

        checkCoreConnectorProperties(c);

        assertNull(c.getUsername());
        assertNull(c.getPassword());
        assertNull(c.getWorkspaceName());
        assertEquals("NONE", c.getContentPayloadType());
        assertEquals(0, c.getEventTypes().intValue());
        assertTrue(c.isDeep());
        assertFalse(c.isNoLocal());

        assertNull(c.getUuids());
        assertNull(c.getNodeTypeNames());
    }

    @Test
    public void testJcrConnectorFullConfiguration() throws Exception
    {
        final JcrConnector c = (JcrConnector) muleContext.getRegistry().lookupConnector(
            "jcrConnectorFullConfiguration");

        checkCoreConnectorProperties(c);

        assertEquals("admin", c.getUsername());
        assertEquals("admin", c.getPassword());
        assertEquals("test", c.getWorkspaceName());
        assertEquals("NOBINARY", c.getContentPayloadType());
        assertEquals(31, c.getEventTypes().intValue());
        assertFalse(c.isDeep());
        assertTrue(c.isNoLocal());

        assertEquals(Arrays.asList(new String[]{"foo", "bar"}), c.getUuids());

        assertEquals(Arrays.asList(new String[]{"oof", "rab"}), c.getNodeTypeNames());
    }

    @Test
    public void testJcrConnectorWithCustomNodeTypeHandlers() throws Exception
    {
        final JcrConnector c = (JcrConnector) muleContext.getRegistry().lookupConnector(
            "jcrConnectorWithCustomNodeTypeHandlers");

        checkCoreConnectorProperties(c);

        assertTrue(c.getNodeTypeHandlerManager().getNodeTypeHandler("nt:query") instanceof NtQueryNodeTypeHandler);
    }

    private void checkCoreConnectorProperties(final JcrConnector c)
    {
        assertNotNull(c);
        assertTrue(c.isConnected());
        assertTrue(c.isStarted());
    }

    @Test
    public void testGlobalJcrEndpointMinimumConfiguration() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getEndpointFactory().getEndpointBuilder(
            "jcrEndpointMinimumConfiguration");

        assertNotNull(endpointBuilder);
        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();
        verifyJcrEndpointMinimumConfiguration("/ref", inboundEndpoint);
    }

    private void verifyJcrEndpointMinimumConfiguration(final String address,
                                                       final ImmutableEndpoint inboundEndpoint)
    {

        assertNotNull(inboundEndpoint);
        assertEquals(address, inboundEndpoint.getEndpointURI().getAddress());

        @SuppressWarnings("unchecked")
        final Map<String, String> props = inboundEndpoint.getProperties();

        assertNull(props.get(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY));
        assertNull(props.get(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY));

        if (inboundEndpoint instanceof InboundEndpoint)
        {
            assertNull(props.get(JcrConnector.JCR_NO_LOCAL_PROPERTY));
            assertNull(props.get(JcrConnector.JCR_DEEP_PROPERTY));
            assertNull(props.get(JcrConnector.JCR_CONTENT_PAYLOAD_TYPE_PROPERTY));
            assertNull(props.get(JcrConnector.JCR_EVENT_TYPES_PROPERTY));
        }
        else
        {
            assertEquals("false", props.get(JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY));
        }

        assertNull(props.get(JcrConnector.JCR_NODE_RELPATH_PROPERTY));
        assertNull(props.get(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY));

        assertNull(props.get(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY));
        assertNull(props.get(JcrConnector.JCR_NODE_UUID_PROPERTY));
        assertNull(JcrNamespaceHandler.split(props.get(JcrConnector.JCR_NODE_TYPE_NAME_LIST_PROPERTY)));
        assertNull(JcrNamespaceHandler.split(props.get(JcrConnector.JCR_UUID_LIST_PROPERTY)));
    }

    @Test
    public void testGlobalJcrEndpointFullConfiguration() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getEndpointFactory().getEndpointBuilder(
            "jcrEndpointFullConfiguration");

        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();

        verifyJcrEndpointFullConfiguration(inboundEndpoint);
    }

    private void verifyJcrEndpointFullConfiguration(final ImmutableEndpoint inboundEndpoint)
    {

        assertNotNull(inboundEndpoint);
        assertEquals("/full", inboundEndpoint.getEndpointURI().getAddress());

        @SuppressWarnings("unchecked")
        final Map<String, String> props = inboundEndpoint.getProperties();

        assertEquals("xpath", props.get(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY));

        assertEquals("/query", props.get(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY));

        if (inboundEndpoint instanceof InboundEndpoint)
        {
            assertEquals("true", props.get(JcrConnector.JCR_NO_LOCAL_PROPERTY));
            assertEquals("false", props.get(JcrConnector.JCR_DEEP_PROPERTY));

            assertEquals("FULL", props.get(JcrConnector.JCR_CONTENT_PAYLOAD_TYPE_PROPERTY));

            assertEquals("4", props.get(JcrConnector.JCR_EVENT_TYPES_PROPERTY));

            assertEquals(Arrays.asList(new String[]{"nt:resource", "nt:unstructured"}),
                JcrNamespaceHandler.split(props.get(JcrConnector.JCR_NODE_TYPE_NAME_LIST_PROPERTY)));

            assertEquals(
                Arrays.asList(new String[]{"f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
                    "e99d4fae-7dec-11d0-a765-00a0c91e6bf6"}),
                JcrNamespaceHandler.split(props.get(JcrConnector.JCR_UUID_LIST_PROPERTY)));
        }
        else
        {
            assertEquals("true", props.get(JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY));

            assertEquals("name", props.get(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY));
        }

        assertEquals("child", props.get(JcrConnector.JCR_NODE_RELPATH_PROPERTY));

        assertEquals("prop", props.get(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY));

        assertEquals("u-u-i-d", props.get(JcrConnector.JCR_NODE_UUID_PROPERTY));

    }

    @Test
    public void testGlobalJcrEndpointAddressConfiguration() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getEndpointFactory().getEndpointBuilder(
            "jcrEndpointAddressConfiguration");

        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();

        assertNotNull(inboundEndpoint);
        assertEquals("/address", inboundEndpoint.getEndpointURI().getAddress());

        @SuppressWarnings("unchecked")
        final Map<String, String> props = inboundEndpoint.getProperties();

        assertEquals("31", props.get(JcrConnector.JCR_EVENT_TYPES_PROPERTY));
    }

    @Test
    public void testGlobalJcrEndpointFilterConfiguration() throws Exception
    {
        final EndpointBuilder endpointBuilder = muleContext.getEndpointFactory().getEndpointBuilder(
            "jcrFilteredConfiguration");

        assertNotNull(endpointBuilder);

        final InboundEndpoint inboundEndpoint = endpointBuilder.buildInboundEndpoint();

        assertNotNull(inboundEndpoint);
        assertEquals("/filtered", inboundEndpoint.getEndpointURI().getAddress());

        assertTrue(inboundEndpoint.getFilter() instanceof AndFilter);

        final AndFilter andFilter = (AndFilter) inboundEndpoint.getFilter();

        final List<Filter> filters = andFilter.getFilters();
        assertEquals(2, filters.size());

        assertTrue(filters.get(0) instanceof JcrNodeNameFilter);
        final JcrNodeNameFilter leftFilter = (JcrNodeNameFilter) filters.get(0);
        assertEquals("jcr:content", leftFilter.getPattern());

        assertTrue(filters.get(1) instanceof JcrPropertyNameFilter);
        final JcrPropertyNameFilter rightFilter = (JcrPropertyNameFilter) filters.get(1);
        assertEquals("jcr:data", rightFilter.getPattern());
    }

    @Test
    public void testGlobalTransformers() throws Exception
    {
        final Transformer e2oT = muleContext.getRegistry().lookupTransformer("EventToObject");

        assertNotNull(e2oT);
        assertTrue(e2oT instanceof JcrEventToObject);

        final Transformer i2oT = muleContext.getRegistry().lookupTransformer("ItemToObject");

        assertNotNull(i2oT);
        assertTrue(i2oT instanceof JcrItemToObject);
    }

    @Test
    public void testServiceEnpointsReferenceConfiguration() throws Exception
    {
        verifyServiceEnpointsMinimumConfiguration("jcrBridgeReferenceConfiguration", "/ref");
    }

    @Test
    public void testServiceEnpointsMinimumConfiguration() throws Exception
    {
        verifyServiceEnpointsMinimumConfiguration("jcrBridgeMinimumConfiguration", "/min");
    }

    @Test
    public void testServiceEnpointsFullConfiguration() throws Exception
    {
        verifyServiceEnpointsFullConfiguration("jcrBridgeFullConfiguration");
    }

    private void verifyServiceEnpointsFullConfiguration(final String serviceName)
    {
        final Service service = (Service) muleContext.getRegistry().lookupObject(serviceName);

        assertNotNull(service);

        verifyJcrEndpointFullConfiguration(((ServiceCompositeMessageSource) service.getMessageSource()).getEndpoints()
            .get(0));

        final OutboundPassThroughRouter optr = (OutboundPassThroughRouter) ((DefaultOutboundRouterCollection) service.getOutboundMessageProcessor()).getRoutes()
            .get(0);

        verifyJcrEndpointFullConfiguration((OutboundEndpoint) optr.getRoutes().get(0));
    }

    public void verifyServiceEnpointsMinimumConfiguration(final String serviceName, final String address)
        throws Exception
    {

        final Service service = (Service) muleContext.getRegistry().lookupObject(serviceName);

        assertNotNull(service);

        verifyJcrEndpointMinimumConfiguration(address,
            ((ServiceCompositeMessageSource) service.getMessageSource()).getEndpoints().get(0));

        final OutboundPassThroughRouter optr = (OutboundPassThroughRouter) ((DefaultOutboundRouterCollection) service.getOutboundMessageProcessor()).getRoutes()
            .get(0);

        verifyJcrEndpointMinimumConfiguration(address, (OutboundEndpoint) optr.getRoutes().get(0));
    }
}
