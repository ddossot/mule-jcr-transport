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
import javax.jcr.nodetype.NodeType;

import org.mule.util.StringUtils;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * TODO comment
 * 
 * @author David Dossot (david@dossot.net)
 */
public class NodeTypeHandlerManager {

	private final ConcurrentHashMap registeredHandlers;

	public NodeTypeHandlerManager() {
		registeredHandlers = new ConcurrentHashMap();
		registerHandler(new NtFileHandler(this));
		registerHandler(new NtLinkedFileHandler(this));
		registerHandler(new NtResourceHandler(this));
		registerHandler(new NtUnstructuredHandler(this));
	}

	public void registerHandler(NodeTypeHandler handler) {
		registeredHandlers.put(handler.getNodeTypeName(), handler);
	}

	public NodeTypeHandler getChildNodeTypeHandler(Node parentNode) {
		try {
			NodeType defaultPrimaryType = parentNode.getDefinition()
					.getDefaultPrimaryType();

			if (defaultPrimaryType == null) {
				throw new IllegalArgumentException(
						"The parent node does not define a default primary type, "
								+ "hence requires a child node type to be specified.");
			}

			return getNodeTypeHandler(defaultPrimaryType.getName());
		} catch (RepositoryException re) {
			throw new RuntimeException(
					"Can not retrieve the default primary type of the parent node!"
							+ "Either specify one or resolve the error condition.",
					re);
		}
	}

	public NodeTypeHandler getNodeTypeHandler(Node node) {
		try {
			return getNodeTypeHandler(node.getPrimaryNodeType().getName());
		} catch (RepositoryException re) {
			throw new RuntimeException(
					"Can not retrieve the primary type of the targeted node!"
							+ "Either specify one or resolve the error condition.",
					re);
		}
	}

	public NodeTypeHandler getNodeTypeHandler(String nodeTypeName) {

		if (StringUtils.isEmpty(nodeTypeName)) {
			throw new IllegalArgumentException(
					"The node type name can not be empty!");
		}

		NodeTypeHandler handler = (NodeTypeHandler) registeredHandlers
				.get(nodeTypeName);

		if (handler == null) {
			handler = new NoActionAnyTypeHandler(this, nodeTypeName);
			registeredHandlers.putIfAbsent(nodeTypeName, handler);
		}

		return handler;
	}

}
