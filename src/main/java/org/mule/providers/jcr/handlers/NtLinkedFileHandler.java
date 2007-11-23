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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.umo.UMOMessage;

/**
 * A nt:linkedFile handler that uses nt:resource for the compulsory jcr:content
 * child node.
 * 
 * @author David Dossot (david@dossot.net)
 */
final class NtLinkedFileHandler extends AbstractNodeTypeHandler {

	public NtLinkedFileHandler(NodeTypeHandlerManager nodeTypeManager) {
		super(nodeTypeManager);
	}

	public String getNodeTypeName() {
		return "nt:linkedFile";
	}

	protected void createChildren(NodeTypeHandlerManager nodeTypeManager,
			Session session, Node node, UMOMessage message)
			throws RepositoryException {

		// no child node
	}

	protected void storeContent(Session session, Node node, UMOMessage message)
			throws RepositoryException {

		Object payload = message.getPayload();

		if (payload instanceof Node) {
			node.setProperty("jcr:content", (Node) payload);
		} else if (payload instanceof String) {
			node.setProperty("jcr:content", session
					.getNodeByUUID((String) payload));
		} else {
			throw new IllegalArgumentException(
					"The content payload for node type "
							+ getNodeTypeName()
							+ " must either be a javax.jcr.Node or a UUID (java.lang.String)");
		}
	}
}
