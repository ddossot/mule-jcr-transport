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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.query.Query;

import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.DispatchException;
import org.mule.api.transport.PropertyScope;
import org.mule.transport.jcr.handlers.NodeTypeHandler;
import org.mule.transport.jcr.handlers.NtQueryNodeTypeHandler;
import org.mule.util.IOUtils;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcherTestCase extends AbstractJcrMessagerTestCase
{

    private JcrMessageDispatcher messageDispatcher;

    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();

        newDispatcherForTestEndpoint(getTestEndpointUri());
    }

    private String getTestEndpointUri()
    {
        return "jcr://" + RepositoryTestSupport.ROOT_NODE_NAME;
    }

    private void newDispatcherForTestEndpoint(final String uri) throws Exception
    {
        final OutboundEndpoint endpoint = JcrEndpointTestCase.newOutboundEndpoint(muleContext, uri, null);
        connector = (JcrConnector) endpoint.getConnector();
        connector.connect();
        connector.start();
        messageDispatcher = (JcrMessageDispatcher) new JcrMessageDispatcherFactory().create(endpoint);
    }

    @Test
    public void testStoreMapInNode() throws Exception
    {
        final Map<String, Object> propertyNameAndValues = new HashMap<String, Object>();
        propertyNameAndValues.put("longProperty", new Long(1234));
        final Calendar now = Calendar.getInstance();
        propertyNameAndValues.put("dateProperty", now);

        final MuleEvent event = getTestEvent(propertyNameAndValues);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        final MuleMessage result = messageDispatcher.doSend(event);
        assertSame(propertyNameAndValues, result.getPayload());
        assertEquals("/testData/noderelpath-target", result.getProperty("itemPath", PropertyScope.INVOCATION));

        final Node node = RepositoryTestSupport.getTestDataNode().getNode("noderelpath-target");

        assertEquals(1234L, node.getProperty("longProperty").getLong());
        assertEquals(now,
            RepositoryTestSupport.getTestDataNode()
                .getNode("noderelpath-target")
                .getProperty("dateProperty")
                .getDate());
    }

    @Test
    public void testFailedStoreInNode() throws Exception
    {
        final MuleEvent event = getTestEvent(new Object());
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);

        RequestContext.setEvent(event);

        try
        {
            messageDispatcher.doSend(event);
        }
        catch (final IllegalArgumentException iae)
        {
            return;
        }

        fail("Should have got an IllegalArgumentException!");
    }

    @Test
    public void testFailedStoreInNodeFromUUID() throws Exception
    {
        final MuleEvent event = getTestEvent(new Object());
        event.getMessage().setProperty(JcrConnector.JCR_NODE_UUID_PROPERTY, "foo-bar-uuid",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "proprelpath-target",
            PropertyScope.INVOCATION);

        RequestContext.setEvent(event);

        try
        {
            messageDispatcher.doSend(event);
        }
        catch (final DispatchException de)
        {
            return;
        }

        fail("Should have got a DispatchException!");
    }

    @Test
    public void testFailedStoreInNodeFromQuery() throws Exception
    {
        final MuleEvent event = getTestEvent(new Object());
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY, "/foo/bar",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath",
            PropertyScope.INVOCATION);

        RequestContext.setEvent(event);

        try
        {
            messageDispatcher.doSend(event);
        }
        catch (final DispatchException de)
        {
            return;
        }

        fail("Should have got a DispatchException!");
    }

    @Test
    public void testStoreCollectionInNode() throws Exception
    {
        final Node node = RepositoryTestSupport.getTestDataNode().getNode("noderelpath-target");

        node.setProperty("multiLongs", new String[]{"0"}, PropertyType.LONG);
        RepositoryTestSupport.getSession().save();

        final List<Long> propertyValues = new ArrayList<Long>();
        propertyValues.add(new Long(1234));
        propertyValues.add(new Long(5678));

        final MuleEvent event = getTestEvent(propertyValues);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "multiLongs",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        final MuleMessage result = messageDispatcher.doSend(event);
        assertSame(propertyValues, result.getPayload());
        assertEquals("/testData/noderelpath-target/multiLongs",
            result.getProperty("itemPath", PropertyScope.INVOCATION));
        assertEquals(2, node.getProperty("multiLongs").getValues().length);
    }

    @Test
    public void testStoreSingleValueInNode() throws Exception
    {
        final Long value = new Long(13579);
        final MuleEvent event = getTestEvent(value);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "proprelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        final MuleMessage result = messageDispatcher.doSend(event);
        assertSame(value, result.getPayload());
        assertEquals("/testData/noderelpath-target/proprelpath-target",
            result.getProperty("itemPath", PropertyScope.INVOCATION));
        assertEquals(value.longValue(), RepositoryTestSupport.getTestDataNode()
            .getNode("noderelpath-target")
            .getProperty("proprelpath-target")
            .getLong());
    }

    @Test
    public void testStoreSingleValueInNodeFromUUID() throws Exception
    {
        final Long value = new Long(4862);
        final MuleEvent event = getTestEvent(value);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_UUID_PROPERTY, uuid, PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "proprelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        assertSame(value, messageDispatcher.doSend(event).getPayload());
        final MuleMessage result = messageDispatcher.doSend(event);
        assertSame(value, result.getPayload());
        assertEquals("/testData/noderelpath-target/proprelpath-target",
            result.getProperty("itemPath", PropertyScope.INVOCATION));
        assertEquals(value.longValue(), RepositoryTestSupport.getTestDataNode()
            .getNode("noderelpath-target")
            .getProperty("proprelpath-target")
            .getLong());
    }

    @Test
    public void testStoreSingleValueInNodeFromQuery() throws Exception
    {
        final Long value = new Long(8246);
        final MuleEvent event = getTestEvent(value);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_STATEMENT_PROPERTY, "//noderelpath-target",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, "xpath",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "proprelpath-target",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        final MuleMessage result = messageDispatcher.doSend(event);
        assertSame(value, result.getPayload());
        assertEquals("/testData/noderelpath-target/proprelpath-target",
            result.getProperty("itemPath", PropertyScope.INVOCATION));
        assertEquals(value.longValue(), RepositoryTestSupport.getTestDataNode()
            .getNode("noderelpath-target")
            .getProperty("proprelpath-target")
            .getLong());
    }

    @Test
    public void testStoreInNewNode() throws Exception
    {
        final MuleEvent event = getTestEvent("bar");
        final String newNodeName = "new-node";
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, newNodeName,
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:resource",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty("jcr:mimeType", "text/plain", PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        final MuleMessage result = messageDispatcher.doSend(event);
        assertNotNull(result.getPayload());
        assertEquals("/testData/new-node", result.getProperty("itemPath", PropertyScope.INVOCATION));
        assertEquals("nt:resource", RepositoryTestSupport.getTestDataNode()
            .getNode(newNodeName)
            .getPrimaryNodeType()
            .getName());
    }

    @Test
    public void testStoreInNewNodeWithDispatch() throws Exception
    {
        final MuleEvent event = getTestEvent("bar");
        final String newNodeName = "new-node-dispatched";
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, newNodeName,
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:resource",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty("jcr:mimeType", "text/plain", PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        messageDispatcher.doDispatch(event);

        assertEquals("nt:resource", RepositoryTestSupport.getTestDataNode()
            .getNode(newNodeName)
            .getPrimaryNodeType()
            .getName());
    }

    @Test
    public void testStoreCustomTypeHandler() throws Exception
    {
        final List<Class<? extends NodeTypeHandler>> customNodeTypeHandlers = new ArrayList<Class<? extends NodeTypeHandler>>();
        customNodeTypeHandlers.add(NtQueryNodeTypeHandler.class);
        connector.setCustomNodeTypeHandlers(customNodeTypeHandlers);

        final MuleEvent event = getTestEvent(null);
        final String newNodeName = "nt-query-node";
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, newNodeName,
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:query",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty("jcr:statement", "/foo/bar", PropertyScope.INVOCATION);
        event.getMessage().setProperty("jcr:language", Query.XPATH, PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        final MuleMessage result = messageDispatcher.doSend(event);
        assertNotNull(result.getPayload());
        assertEquals("/testData/nt-query-node", result.getProperty("itemPath", PropertyScope.INVOCATION));

        final Node node = RepositoryTestSupport.getTestDataNode().getNode(newNodeName);

        assertEquals("nt:query", node.getPrimaryNodeType().getName());
        assertEquals(Query.XPATH, node.getProperty("jcr:language").getString());
        assertEquals("/foo/bar", node.getProperty("jcr:statement").getString());
    }

    @Test
    public void testStoreInForcedNewNode() throws Exception
    {
        final MuleEvent event = getTestEvent("bar");
        event.getMessage().setProperty(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "new-forced-node",
            PropertyScope.INVOCATION);
        event.getMessage().setProperty(JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY, "true",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        MuleMessage result = messageDispatcher.doSend(event);
        assertNotNull(result.getPayload());
        assertEquals("/testData/new-forced-node", result.getProperty("itemPath", PropertyScope.INVOCATION));

        result = messageDispatcher.doSend(event);
        assertNotNull(result.getPayload());
        assertEquals("/testData/new-forced-node[2]", result.getProperty("itemPath", PropertyScope.INVOCATION));

        assertEquals(2, RepositoryTestSupport.getTestDataNode().getNodes("new-forced-node").getSize());
    }

    @Test
    public void testFailedStoreUnderProperty() throws Exception
    {
        newDispatcherForTestEndpoint("jcr://" + RepositoryTestSupport.ROOT_NODE_NAME + "/pi");

        final MuleEvent event = getTestEvent("bar");
        event.getMessage().setProperty(JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY, "true",
            PropertyScope.INVOCATION);
        RequestContext.setEvent(event);

        try
        {
            messageDispatcher.doSend(event).getPayload();
        }
        catch (final IllegalArgumentException iae)
        {
            return;
        }

        fail("Should have got an IAE");
    }

    @Test
    public void testConnectorGetOutputStream() throws Exception
    {
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "stored-stream");
        properties.put(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:resource");
        properties.put("jcr:mimeType", "text/plain");

        final MuleMessage message = new DefaultMuleMessage(null, properties, muleContext);

        final MuleEvent event = getTestEvent(message,
            getTestInboundEndpoint("test", getTestEndpointUri(), null, null, null, connector));

        RequestContext.setEvent(event);

        final OutputStream outputStream = connector.getOutputStream(messageDispatcher.getEndpoint(), event);

        assertNotNull(outputStream);

        final String testContent = "test streamed content";
        IOUtils.copy(new StringReader(testContent), outputStream);

        outputStream.flush();
        outputStream.close();

        int attempt = 0;
        Property property = null;
        while (property == null && attempt < 20)
        {
            attempt++;

            try
            {
                property = RepositoryTestSupport.getTestDataNode()
                    .getNode("stored-stream")
                    .getProperty("jcr:data");
            }
            catch (final PathNotFoundException pnfe)
            {
                System.out.println("Pondering on JCR property change: attempt #" + attempt);
                Thread.sleep(500L);
            }
        }

        assertNotNull(property);

        final StringWriter sw = new StringWriter();
        IOUtils.copy(property.getStream(), sw);

        assertEquals(testContent, sw.toString());
    }

}
