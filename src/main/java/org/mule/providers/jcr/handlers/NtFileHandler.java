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
 * A nt:file handler that uses nt:resource for the compulsory jcr:content child
 * node.
 * 
 * @author David Dossot (david@dossot.net)
 */
final class NtFileHandler extends AbstractNodeTypeHandler {

	public NtFileHandler(NodeTypeHandlerManager nodeTypeManager) {
		super(nodeTypeManager);
	}

	public String getNodeTypeName() {
		return "nt:file";
	}

	protected void createChildren(NodeTypeHandlerManager nodeTypeManager,
			Session session, Node node, UMOMessage message)
			throws RepositoryException, IOException {

		nodeTypeManager.getNodeTypeHandler("nt:resource").newNode(session,
				node, "jcr:content", message);
	}

	protected void storeContent(Session session, Node node, UMOMessage message)
			throws RepositoryException {
		// No content is stored in that type of node
	}

}
