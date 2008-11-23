/**
 * 
 */
package org.mule.transport.jcr.handlers;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.api.MuleMessage;
import org.mule.transport.jcr.handlers.NodeTypeHandler;
import org.mule.transport.jcr.handlers.NodeTypeHandlerManager;

public final class NtQueryNodeTypeHandler implements NodeTypeHandler {

    public String getNodeTypeName() {
        return "nt:query";
    }

    public Node createNode(final Session session, final Node targetNode,
            final String nodeRelPath, final MuleMessage message)
            throws RepositoryException, IOException {

        final Node node = targetNode
                .addNode(nodeRelPath, getNodeTypeName());
        updateContent(session, node, message);
        return node;
    }

    public void updateContent(final Session session, final Node node,
            final MuleMessage message) throws RepositoryException,
            IOException {
        node.setProperty("jcr:statement", message.getStringProperty(
                "jcr:statement", null));
        node.setProperty("jcr:language", message.getStringProperty(
                "jcr:language", null));
    }

    public void initialize(final NodeTypeHandlerManager manager) {
        // ignore the manager
    }
}