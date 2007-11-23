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
 * Parent class and prototype for the implementations of
 * <code>NodeTypeHandler</code> provided with the JCR provider.
 * 
 * @author David Dossot (david@dossot.net)
 */
abstract class AbstractNodeTypeHandler implements NodeTypeHandler {

	private final NodeTypeHandlerManager nodeTypeManager;

	public AbstractNodeTypeHandler(NodeTypeHandlerManager nodeTypeManager) {
		this.nodeTypeManager = nodeTypeManager;
	}

	public final Node newNode(Session session, Node targetNode,
			String nodeRelPath, UMOMessage message) throws RepositoryException,
			IOException {

		Node node = targetNode.addNode(nodeRelPath, getNodeTypeName());
		storeContent(session, node, message);
		createChildren(nodeTypeManager, session, node, message);
		return node;
	}

	protected abstract void storeContent(Session session, Node node,
			UMOMessage message) throws RepositoryException, IOException;

	protected abstract void createChildren(
			NodeTypeHandlerManager nodeTypeManager, Session session, Node node,
			UMOMessage message) throws RepositoryException, IOException;

}
