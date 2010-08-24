/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr.handlers;

import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.transport.PropertyScope;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transport.jcr.RepositoryTestSupport;
import org.mule.util.StringUtils;

/**
 * @author David Dossot (david@dossot.net)
 */
public class NodeTypeHandlersTest extends AbstractMuleTestCase
{
    private static final String ORIGINAL_RESOURCE_NODE_CONTENT = "foo-resource";

    private static final String MODIFIED_RESOURCE_NODE_CONTENT = "resource-bar";

    private static final String IGNORED_FOLDER_CONTENT = "foo-folder";

    private static final String ORIGINAL_UNSTRUCTURED_NODE_CONTENT = "foo-unstructured";

    private static final String MODIFIED_UNSTRUCTURED_NODE_CONTENT = "unstructured-bar";

    private static final String ORIGINAL_DEFAULT_NODE_CONTENT = "foo-defaultType";

    private static final String MODIFIED_DEFAULT_NODE_CONTENT = "defaultType-bar";

    private static final String ORIGINAL_FILE_NODE_CONTENT = "foo-file";

    private static final String MODIFIED_FILE_NODE_CONTENT = "file-bar";

    /**
     * We trust JackRabbit to enfore node type content and hierarchy so we merely
     * check that values we store and update are taken in account. This looks brutal
     * but does a pretty good job anyway :-)
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void testAll() throws Exception
    {
        super.setUp();
        final Session session = RepositoryTestSupport.getSession();

        final Node testDataNode = RepositoryTestSupport.getTestDataNode();

        final NodeTypeHandlerManager nodeTypeManager = new NodeTypeHandlerManager();

        final Node defaultNode = nodeTypeManager.getChildNodeTypeHandler(testDataNode).createNode(session,
            testDataNode, "defaultNode",
            new DefaultMuleMessage(ORIGINAL_DEFAULT_NODE_CONTENT, Collections.EMPTY_MAP, muleContext));

        final Node unstructuredNode = nodeTypeManager.getNodeTypeHandler("nt:unstructured").createNode(
            session, testDataNode, "unstructuredNode",
            new DefaultMuleMessage(ORIGINAL_UNSTRUCTURED_NODE_CONTENT, Collections.EMPTY_MAP, muleContext));

        MuleMessage msg = new DefaultMuleMessage(ORIGINAL_FILE_NODE_CONTENT, Collections.EMPTY_MAP,
            muleContext);

        msg.setProperty("jcr:mimeType", "text/plain", PropertyScope.INVOCATION);
        final Node fileNode = nodeTypeManager.getNodeTypeHandler("nt:file").createNode(session, testDataNode,
            "fileNode", msg);
        final Node fileContentNode = fileNode.getNode("jcr:content");

        msg = new DefaultMuleMessage(fileContentNode, Collections.EMPTY_MAP, muleContext);
        nodeTypeManager.getNodeTypeHandler("nt:linkedFile").createNode(session, testDataNode,
            "linkedFileNodeFromNode", msg);

        msg = new DefaultMuleMessage(fileContentNode.getUUID(), Collections.EMPTY_MAP, muleContext);
        nodeTypeManager.getNodeTypeHandler("nt:linkedFile").createNode(session, testDataNode,
            "linkedFileNodeFromUUID", msg);

        msg = new DefaultMuleMessage(IGNORED_FOLDER_CONTENT, Collections.EMPTY_MAP, muleContext);
        final Node folderNode = nodeTypeManager.getNodeTypeHandler("nt:folder").createNode(session,
            testDataNode, "folderNode", msg);

        msg = new DefaultMuleMessage(ORIGINAL_RESOURCE_NODE_CONTENT, Collections.EMPTY_MAP, muleContext);
        msg.setProperty("jcr:mimeType", "text/plain", PropertyScope.INVOCATION);
        final Node resourceNode = nodeTypeManager.getNodeTypeHandler("nt:resource").createNode(session,
            testDataNode, "resourceNode", msg);

        session.save();
        String dumpResult = dump(testDataNode);

        assertEquals(1, StringUtils.countMatches(dumpResult, ORIGINAL_DEFAULT_NODE_CONTENT));
        assertEquals(0, StringUtils.countMatches(dumpResult, MODIFIED_DEFAULT_NODE_CONTENT));

        assertEquals(1, StringUtils.countMatches(dumpResult, ORIGINAL_UNSTRUCTURED_NODE_CONTENT));
        assertEquals(0, StringUtils.countMatches(dumpResult, MODIFIED_UNSTRUCTURED_NODE_CONTENT));

        assertEquals(1, StringUtils.countMatches(dumpResult, ORIGINAL_FILE_NODE_CONTENT));
        assertEquals(0, StringUtils.countMatches(dumpResult, MODIFIED_FILE_NODE_CONTENT));

        assertEquals(0, StringUtils.countMatches(dumpResult, IGNORED_FOLDER_CONTENT));

        assertEquals(1, StringUtils.countMatches(dumpResult, ORIGINAL_RESOURCE_NODE_CONTENT));
        assertEquals(0, StringUtils.countMatches(dumpResult, MODIFIED_RESOURCE_NODE_CONTENT));

        // -- modify a few nodes --

        nodeTypeManager.getNodeTypeHandler(defaultNode).updateContent(session, defaultNode,
            new DefaultMuleMessage(MODIFIED_DEFAULT_NODE_CONTENT, Collections.EMPTY_MAP, muleContext));

        nodeTypeManager.getNodeTypeHandler(unstructuredNode).updateContent(session, unstructuredNode,
            new DefaultMuleMessage(MODIFIED_UNSTRUCTURED_NODE_CONTENT, Collections.EMPTY_MAP, muleContext));

        msg = new DefaultMuleMessage(MODIFIED_FILE_NODE_CONTENT, Collections.EMPTY_MAP, muleContext);
        msg.setProperty("jcr:mimeType", "text/plain", PropertyScope.INVOCATION);
        nodeTypeManager.getNodeTypeHandler(fileNode).updateContent(session, fileNode, msg);

        nodeTypeManager.getNodeTypeHandler(folderNode).updateContent(session, folderNode,
            new DefaultMuleMessage(IGNORED_FOLDER_CONTENT, Collections.EMPTY_MAP, muleContext));

        msg = new DefaultMuleMessage(MODIFIED_RESOURCE_NODE_CONTENT, Collections.EMPTY_MAP, muleContext);
        msg.setProperty("jcr:mimeType", "text/plain", PropertyScope.INVOCATION);
        nodeTypeManager.getNodeTypeHandler(resourceNode).updateContent(session, resourceNode, msg);

        session.save();

        dumpResult = dump(testDataNode);

        assertEquals(0, StringUtils.countMatches(dumpResult, ORIGINAL_DEFAULT_NODE_CONTENT));
        assertEquals(1, StringUtils.countMatches(dumpResult, MODIFIED_DEFAULT_NODE_CONTENT));

        assertEquals(0, StringUtils.countMatches(dumpResult, ORIGINAL_UNSTRUCTURED_NODE_CONTENT));
        assertEquals(1, StringUtils.countMatches(dumpResult, MODIFIED_UNSTRUCTURED_NODE_CONTENT));

        assertEquals(0, StringUtils.countMatches(dumpResult, ORIGINAL_FILE_NODE_CONTENT));
        assertEquals(1, StringUtils.countMatches(dumpResult, MODIFIED_FILE_NODE_CONTENT));

        assertEquals(0, StringUtils.countMatches(dumpResult, IGNORED_FOLDER_CONTENT));

        assertEquals(0, StringUtils.countMatches(dumpResult, ORIGINAL_RESOURCE_NODE_CONTENT));
        assertEquals(1, StringUtils.countMatches(dumpResult, MODIFIED_RESOURCE_NODE_CONTENT));
    }

    /** Recursively outputs the contents of the given node. */
    private static String dump(final Node node) throws RepositoryException
    {
        // First output the node path
        final StringBuffer sb = new StringBuffer(node.getPath()).append("\n");

        // Skip the virtual (and large!) jcr:system subtree
        if (node.getName().equals("jcr:system"))
        {
            return sb.toString();
        }

        // Then output the properties
        final PropertyIterator properties = node.getProperties();
        while (properties.hasNext())
        {
            final Property property = properties.nextProperty();
            if (property.getDefinition().isMultiple())
            {
                // A multi-valued property, print all values
                final Value[] values = property.getValues();
                for (int i = 0; i < values.length; i++)
                {
                    sb.append(property.getPath()).append(" = ").append(values[i].getString()).append("\n");
                }
            }
            else
            {
                // A single-valued property
                sb.append(property.getPath()).append(" = ").append(property.getString()).append("\n");
            }
        }

        // Finally output all the child nodes recursively
        final NodeIterator nodes = node.getNodes();
        while (nodes.hasNext())
        {
            sb.append(dump(nodes.nextNode()));
        }

        return sb.toString();
    }
}
