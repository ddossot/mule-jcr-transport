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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.mule.impl.MuleMessage;
import org.mule.impl.RequestContext;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.providers.NullPayload;
import org.mule.providers.jcr.filters.JcrNodeNameFilter;
import org.mule.providers.jcr.filters.JcrPropertyNameFilter;
import org.mule.providers.jcr.handlers.NodeTypeHandler;
import org.mule.providers.jcr.handlers.NodeTypeHandlerManager;
import org.mule.providers.streaming.StreamMessageAdapter;
import org.mule.routing.filters.logic.AndFilter;
import org.mule.routing.filters.logic.NotFilter;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;
import org.mule.umo.provider.DispatchException;
import org.mule.util.IOUtils;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcherTestCase extends AbstractMuleTestCase {

    public static final class NtQueryNodeTypeHandler implements NodeTypeHandler {
        public String getNodeTypeName() {
            return "nt:query";
        }

        public Node createNode(Session session, Node targetNode,
                String nodeRelPath, UMOMessage message)
                throws RepositoryException, IOException {

            Node node = targetNode.addNode(nodeRelPath, getNodeTypeName());
            updateContent(session, node, message);
            return node;
        }

        public void updateContent(Session session, Node node, UMOMessage message)
                throws RepositoryException, IOException {
            node.setProperty("jcr:statement", message.getStringProperty(
                    "jcr:statement", null));
            node.setProperty("jcr:language", message.getStringProperty(
                    "jcr:language", null));
        }

        public void initialize(NodeTypeHandlerManager manager) {
            // ignore the manager
        }
    }

    private JcrConnector connector;

    private JcrMessageDispatcher messageDispatcher;

    private MuleEndpoint endpoint;

    private String uuid;

    protected void doSetUp() throws Exception {
        super.doSetUp();

        connector = JcrConnectorTestCase.newJcrConnector();

        endpoint = new MuleEndpoint("jcr://"
            + RepositoryTestSupport.ROOT_NODE_NAME, true);

        endpoint.setConnector(connector);

        endpoint.setStreaming(false);

        messageDispatcher =
                (JcrMessageDispatcher) new JcrMessageDispatcherFactory().create(endpoint);

        // create some extra test nodes and properties
        RepositoryTestSupport.resetRepository();

        Node testDataNode = RepositoryTestSupport.getTestDataNode();
        testDataNode.setProperty("foo", Math.PI);
        testDataNode.setProperty("stream", new ByteArrayInputStream(
                "test".getBytes()));
        testDataNode.setProperty("text", "EHLO SPAM");

        Node target = testDataNode.addNode("noderelpath-target");
        target.setProperty("proprelpath-target", 123L);

        testDataNode.addMixin("mix:referenceable");
        uuid = testDataNode.getUUID();

        RepositoryTestSupport.getSession().save();
    }

    protected void doTearDown() throws Exception {
        RequestContext.setEvent(null);

        connector.disconnect();
        connector.dispose();
        super.doTearDown();
    }

    public void testReceiveInputStreamNonStreamingEndpoint() throws Exception {
        JcrPropertyNameFilter jcrPropertyNameFilter =
                new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("stream");
        setFilter(jcrPropertyNameFilter);

        UMOMessage received = messageDispatcher.receive(0);
        assertFalse(received.getAdapter() instanceof StreamMessageAdapter);
        assertTrue(received.getPayload() instanceof InputStream);
    }

    public void testReceiveInputStreamStreamingEndpoint() throws Exception {
        endpoint.setStreaming(true);
        JcrPropertyNameFilter jcrPropertyNameFilter =
                new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("stream");
        setFilter(jcrPropertyNameFilter);

        UMOMessage received = messageDispatcher.receive(0);
        assertTrue(received.getAdapter() instanceof StreamMessageAdapter);
        assertTrue(received.getPayload() instanceof InputStream);
    }

    public void testReceiveNonInputStreamStreamingEndpoint() throws Exception {
        endpoint.setStreaming(true);
        JcrPropertyNameFilter jcrPropertyNameFilter =
                new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("text");
        setFilter(jcrPropertyNameFilter);

        UMOMessage received = messageDispatcher.receive(0);
        assertTrue(received.getAdapter() instanceof StreamMessageAdapter);
        assertTrue(received.getPayload() instanceof InputStream);
    }

    public void testReceiveWithEventUUID() throws Exception {
        UMOEvent event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
        RequestContext.setEvent(event);

        assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);

        event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_UUID_PROPERTY, "foo");
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(),
                messageDispatcher.receive(0).getPayload());
    }

    public void testReceiveWithEventUUIDAndNodeRelpath() throws Exception {
        UMOEvent event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
        RequestContext.setEvent(event);

        assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);

        event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "bar");
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(),
                messageDispatcher.receive(0).getPayload());
    }

    public void testReceiveWithEventUUIDAndPropertyRelpath() throws Exception {
        UMOEvent event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "foo");
        RequestContext.setEvent(event);

        assertEquals(Double.class,
                messageDispatcher.receive(0).getPayload().getClass());

        event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "bar");
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(),
                messageDispatcher.receive(0).getPayload());
    }

    public void testReceiveWithEventNodeRelpath() throws Exception {
        UMOEvent event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
        RequestContext.setEvent(event);

        assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);

        event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "bar");
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(),
                messageDispatcher.receive(0).getPayload());
    }

    public void testReceiveWithEventPropertyRelpath() throws Exception {
        UMOEvent event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "foo");
        RequestContext.setEvent(event);

        assertEquals(Double.class,
                messageDispatcher.receive(0).getPayload().getClass());

        event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "bar");
        RequestContext.setEvent(event);

        assertEquals(NullPayload.getInstance(),
                messageDispatcher.receive(0).getPayload());
    }

    public void testReceiveWithEventNodeAndPropertyRelpath() throws Exception {
        UMOEvent event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
                "proprelpath-target");
        RequestContext.setEvent(event);

        assertEquals(Long.class,
                messageDispatcher.receive(0).getPayload().getClass());
    }

    public void testReceiveWithEventNodeAndPropertyRelpathFromRoot()
            throws Exception {
        endpoint = new MuleEndpoint("jcr:///", true);

        endpoint.setConnector(connector);

        messageDispatcher =
                (JcrMessageDispatcher) new JcrMessageDispatcherFactory().create(endpoint);

        UMOEvent event = getTestEvent(null);

        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY,
                RepositoryTestSupport.ROOT_NODE_NAME
                    + "/noderelpath-target");

        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
                "proprelpath-target");

        RequestContext.setEvent(event);

        assertEquals(Long.class,
                messageDispatcher.receive(0).getPayload().getClass());
    }

    public void testReceiveByEndpointUriNoFilter() throws Exception {
        assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);
    }

    public void testReceiveByEndpointUriWithBadFilter() throws Exception {

        try {
            setFilter(new NotFilter());
        } catch (IllegalArgumentException iae) {
            return;
        }

        fail("should have got an IAE");
    }

    public void testReceiveByEndpointUriWithPropertyNameFilter()
            throws Exception {
        JcrPropertyNameFilter jcrPropertyNameFilter =
                new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("fo*");
        setFilter(jcrPropertyNameFilter);
        assertEquals(Double.class,
                messageDispatcher.receive(0).getPayload().getClass());

        jcrPropertyNameFilter = new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("bar");
        setFilter(jcrPropertyNameFilter);
        assertEquals(NullPayload.getInstance(),
                messageDispatcher.receive(0).getPayload());
    }

    public void testReceiveByEndpointUriWithNodeNameFilter() throws Exception {
        JcrNodeNameFilter jcrNodeNameFilter = new JcrNodeNameFilter();
        jcrNodeNameFilter.setPattern("*-target");
        setFilter(jcrNodeNameFilter);
        assertFalse(NullPayload.getInstance().equals(
                messageDispatcher.receive(0).getPayload()));

        jcrNodeNameFilter = new JcrNodeNameFilter();
        jcrNodeNameFilter.setPattern("noderelpath-*");
        setFilter(jcrNodeNameFilter);
        assertTrue(messageDispatcher.receive(0).getPayload() instanceof Map);

        jcrNodeNameFilter = new JcrNodeNameFilter();
        jcrNodeNameFilter.setPattern("bar");
        setFilter(jcrNodeNameFilter);
        assertEquals(NullPayload.getInstance(),
                messageDispatcher.receive(0).getPayload());
    }

    public void testReceiveByEndpointUriWithBothNameFilters() throws Exception {
        JcrNodeNameFilter jcrNodeNameFilter = new JcrNodeNameFilter();
        jcrNodeNameFilter.setPattern("noderelpath-*");

        JcrPropertyNameFilter jcrPropertyNameFilter =
                new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("proprelpath-**");

        AndFilter andFilter =
                new AndFilter(jcrNodeNameFilter, jcrPropertyNameFilter);

        setFilter(andFilter);

        assertEquals(Long.class,
                messageDispatcher.receive(0).getPayload().getClass());
    }

    public void testReceiveWithEventNodeRelpathAndPropertyFilter()
            throws Exception {

        UMOEvent event = getTestEvent(null);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
        RequestContext.setEvent(event);

        JcrPropertyNameFilter jcrPropertyNameFilter =
                new JcrPropertyNameFilter();
        jcrPropertyNameFilter.setPattern("proprelpath-**");
        setFilter(jcrPropertyNameFilter);

        assertEquals(Long.class,
                messageDispatcher.receive(0).getPayload().getClass());
    }

    public void testStoreMapInNode() throws Exception {
        Map propertyNameAndValues = new HashMap();
        propertyNameAndValues.put("longProperty", new Long(1234));
        Calendar now = Calendar.getInstance();
        propertyNameAndValues.put("dateProperty", now);

        UMOEvent event = getTestEvent(propertyNameAndValues);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
        RequestContext.setEvent(event);

        assertSame(propertyNameAndValues,
                messageDispatcher.doSend(event).getPayload());

        Node node =
                RepositoryTestSupport.getTestDataNode().getNode(
                        "noderelpath-target");

        assertEquals(1234L, node.getProperty("longProperty").getLong());
        assertEquals(now, RepositoryTestSupport.getTestDataNode().getNode(
                "noderelpath-target").getProperty("dateProperty").getDate());
    }

    public void testFailedStoreInNode() throws Exception {
        UMOEvent event = getTestEvent(new Object());
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");

        RequestContext.setEvent(event);

        try {
            messageDispatcher.doSend(event);
        } catch (IllegalArgumentException iae) {
            return;
        }

        fail("Should have got an IllegalArgumentException!");
    }

    public void testFailedStoreInNodeFromUUID() throws Exception {
        UMOEvent event = getTestEvent(new Object());
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_UUID_PROPERTY, "foo-bar-uuid");
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
                "proprelpath-target");

        RequestContext.setEvent(event);

        try {
            messageDispatcher.doSend(event);
        } catch (DispatchException de) {
            return;
        }

        fail("Should have got a DispatchException!");
    }

    public void testStoreCollectionInNode() throws Exception {
        Node node =
                RepositoryTestSupport.getTestDataNode().getNode(
                        "noderelpath-target");

        node.setProperty("multiLongs", new String[] { "0" }, PropertyType.LONG);
        RepositoryTestSupport.getSession().save();

        List propertyValues = new ArrayList();
        propertyValues.add(new Long(1234));
        propertyValues.add(new Long(5678));

        UMOEvent event = getTestEvent(propertyValues);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, "multiLongs");
        RequestContext.setEvent(event);

        assertSame(propertyValues, messageDispatcher.doSend(event).getPayload());
        assertEquals(2, node.getProperty("multiLongs").getValues().length);
    }

    public void testStoreSingleValueInNode() throws Exception {
        Long value = new Long(13579);
        UMOEvent event = getTestEvent(value);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
                "proprelpath-target");
        RequestContext.setEvent(event);

        assertSame(value, messageDispatcher.doSend(event).getPayload());
        assertEquals(
                value.longValue(),
                RepositoryTestSupport.getTestDataNode().getNode(
                        "noderelpath-target").getProperty("proprelpath-target").getLong());
    }

    public void testStoreSingleValueInNodeFromUUID() throws Exception {
        Long value = new Long(4862);
        UMOEvent event = getTestEvent(value);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_UUID_PROPERTY, uuid);
        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "noderelpath-target");
        event.getMessage().setStringProperty(
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY,
                "proprelpath-target");
        RequestContext.setEvent(event);

        assertSame(value, messageDispatcher.doSend(event).getPayload());
        assertEquals(
                value.longValue(),
                RepositoryTestSupport.getTestDataNode().getNode(
                        "noderelpath-target").getProperty("proprelpath-target").getLong());
    }

    public void testStoreInNewNode() throws Exception {
        UMOEvent event = getTestEvent("bar");

        String newNodeName = "new-node";

        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, newNodeName);

        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:resource");

        event.getMessage().setStringProperty("jcr:mimeType", "text/plain");

        RequestContext.setEvent(event);

        assertNotNull(messageDispatcher.doSend(event).getPayload());

        assertEquals(
                "nt:resource",
                RepositoryTestSupport.getTestDataNode().getNode(newNodeName).getPrimaryNodeType().getName());
    }

    public void testStoreInNewNodeWithDispatch() throws Exception {
        UMOEvent event = getTestEvent("bar");

        String newNodeName = "new-node-dispatched";

        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, newNodeName);

        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:resource");

        event.getMessage().setStringProperty("jcr:mimeType", "text/plain");

        RequestContext.setEvent(event);

        messageDispatcher.doDispatch(event);

        assertEquals(
                "nt:resource",
                RepositoryTestSupport.getTestDataNode().getNode(newNodeName).getPrimaryNodeType().getName());
    }

    public void testStoreCustomTypeHandler() throws Exception {
        connector.setCustomNodeTypeHandlers(Collections.singletonList(NtQueryNodeTypeHandler.class.getName()));

        UMOEvent event = getTestEvent(null);

        String newNodeName = "nt-query-node";

        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, newNodeName);

        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:query");

        event.getMessage().setStringProperty("jcr:statement", "/foo/bar");
        event.getMessage().setStringProperty("jcr:language", Query.XPATH);

        RequestContext.setEvent(event);

        assertNotNull(messageDispatcher.doSend(event).getPayload());

        Node node =
                RepositoryTestSupport.getTestDataNode().getNode(newNodeName);

        assertEquals("nt:query", node.getPrimaryNodeType().getName());
        assertEquals(Query.XPATH, node.getProperty("jcr:language").getString());
        assertEquals("/foo/bar", node.getProperty("jcr:statement").getString());
    }

    public void testStoreInForcedNewNode() throws Exception {
        UMOEvent event = getTestEvent("bar");

        event.getMessage().setStringProperty(
                JcrConnector.JCR_NODE_RELPATH_PROPERTY, "new-forced-node");

        event.getMessage().setStringProperty(
                JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY, "true");

        RequestContext.setEvent(event);

        assertNotNull(messageDispatcher.doSend(event).getPayload());
        assertNotNull(messageDispatcher.doSend(event).getPayload());

        assertEquals(2, RepositoryTestSupport.getTestDataNode().getNodes(
                "new-forced-node").getSize());
    }

    public void testFailedStoreUnderProperty() throws Exception {
        endpoint = new MuleEndpoint("jcr://"
            + RepositoryTestSupport.ROOT_NODE_NAME
                + "/foo", true);

        endpoint.setConnector(connector);

        messageDispatcher =
                (JcrMessageDispatcher) new JcrMessageDispatcherFactory().create(endpoint);

        UMOEvent event = getTestEvent("bar");
        event.getMessage().setStringProperty(
                JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY, "true");
        RequestContext.setEvent(event);

        try {
            messageDispatcher.doSend(event).getPayload();
        } catch (IllegalArgumentException iae) {
            return;
        }

        fail("Should have got an IAE");
    }

    public void testConnectorGetOutputStream() throws Exception {
        Map properties = new HashMap();
        properties.put(JcrConnector.JCR_NODE_RELPATH_PROPERTY, "stored-stream");
        properties.put(JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, "nt:resource");
        properties.put("jcr:mimeType", "text/plain");

        MuleMessage message = new MuleMessage(null, properties);

        OutputStream outputStream =
                connector.getOutputStream(endpoint, message);

        assertNotNull(outputStream);

        final String testContent = "test streamed content";
        IOUtils.copy(new StringReader(testContent), outputStream);

        outputStream.flush();
        outputStream.close();

        Property property =
                RepositoryTestSupport.getTestDataNode().getNode("stored-stream").getProperty(
                        "jcr:data");

        assertNotNull(property);

        StringWriter sw = new StringWriter();
        IOUtils.copy(property.getStream(), sw);

        assertEquals(testContent, sw.toString());
    }

    private void setFilter(UMOFilter filter) {
        endpoint.setFilter(filter);
        messageDispatcher.refreshEndpointFilter();
    }
}
