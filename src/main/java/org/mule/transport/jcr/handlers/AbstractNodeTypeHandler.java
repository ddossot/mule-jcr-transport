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

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.api.MuleMessage;

/**
 * Parent class and prototype for the implementations of
 * <code>NodeTypeHandler</code> provided with the JCR provider.
 * 
 * @author David Dossot (david@dossot.net)
 */
abstract class AbstractNodeTypeHandler implements NodeTypeHandler {

	private NodeTypeHandlerManager nodeTypeManager;

	public void initialize(final NodeTypeHandlerManager nodeTypeManager) {
		this.nodeTypeManager = nodeTypeManager;
	}

	protected NodeTypeHandlerManager getNodeTypeManager() {
		return nodeTypeManager;
	}

	public final Node createNode(final Session session, final Node targetNode,
			final String nodeRelPath, MuleMessage message)
			throws RepositoryException, IOException {

		final Node node = targetNode.addNode(nodeRelPath, getNodeTypeName());
		createChildren(node);
		updateContent(session, node, message);
		return node;
	}

	protected abstract void createChildren(Node node)
			throws RepositoryException;

}
