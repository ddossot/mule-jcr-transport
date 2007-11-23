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
 * TODO comment
 * 
 * @author David Dossot (david@dossot.net)
 */
final class NoActionAnyTypeHandler extends AbstractNodeTypeHandler {

	private final String nodeTypeName;

	public NoActionAnyTypeHandler(NodeTypeHandlerManager nodeTypeManager,
			String nodeTypeName) {
		super(nodeTypeManager);
		this.nodeTypeName = nodeTypeName;
	}

	public String getNodeTypeName() {
		return nodeTypeName;
	}

	protected void createChildren(NodeTypeHandlerManager nodeTypeManager,
			Session session, Node node, UMOMessage message)
			throws RepositoryException {
		// no children to create
	}

	protected void storeContent(Session session, Node node, UMOMessage message)
			throws RepositoryException {
		// do not store anything
		// TODO warn
	}

}
