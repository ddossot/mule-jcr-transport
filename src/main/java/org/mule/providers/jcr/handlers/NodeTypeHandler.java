/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr.handlers;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.umo.UMOMessage;

/**
 * Defines a handler that extracts the necessary information from a
 * <code>UMOMessage</code> in order to store content in a JCR container while
 * trying to comply to the constraint of the desired node type.
 * 
 * @author David Dossot (david@dossot.net)
 */
public interface NodeTypeHandler {

    /**
     * Called once, when the handler is created.
     * 
     * @param nodeTypeManager
     */
    void initialize(NodeTypeHandlerManager manager);

    /**
     * The node type name that will be used to register to the manager. It does
     * not necessarily map with an actual JCR node name.
     * 
     * @return
     */
    String getNodeTypeName();

    /**
     * Handles the creation of a new node.
     * 
     * @param session
     * @param targetNode
     * @param nodeRelPath
     * @param message
     * @return
     * @throws RepositoryException
     * @throws IOException
     */
    Node createNode(Session session, Node targetNode, String nodeRelPath,
            UMOMessage message) throws RepositoryException, IOException;

    /**
     * Handles a node content update.
     * 
     * @param session
     * @param node
     * @param message
     * @throws RepositoryException
     * @throws IOException
     */
    void updateContent(Session session, Node node, UMOMessage message)
            throws RepositoryException, IOException;

}
