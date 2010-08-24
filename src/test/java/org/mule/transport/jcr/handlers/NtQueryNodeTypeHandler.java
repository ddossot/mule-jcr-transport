
package org.mule.transport.jcr.handlers;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.api.MuleMessage;

public final class NtQueryNodeTypeHandler implements NodeTypeHandler
{

    public String getNodeTypeName()
    {
        return "nt:query";
    }

    public Node createNode(final Session session,
                           final Node targetNode,
                           final String nodeRelPath,
                           final MuleMessage message) throws RepositoryException, IOException
    {

        final Node node = targetNode.addNode(nodeRelPath, getNodeTypeName());
        updateContent(session, node, message);
        return node;
    }

    public void updateContent(final Session session, final Node node, final MuleMessage message)
        throws RepositoryException, IOException
    {
        node.setProperty("jcr:statement", (String) message.findPropertyInAnyScope("jcr:statement", null));
        node.setProperty("jcr:language", (String) message.findPropertyInAnyScope("jcr:language", null));
    }

    public void initialize(final NodeTypeHandlerManager manager)
    {
        // ignore the manager
    }
}
