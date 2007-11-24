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
import java.util.Collection;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.providers.jcr.JcrMessageUtils;
import org.mule.umo.UMOMessage;

/**
 * TODO comment
 * 
 * @author David Dossot (david@dossot.net)
 */
final class NtUnstructuredHandler extends AbstractNodeTypeHandler {

	public NtUnstructuredHandler(NodeTypeHandlerManager nodeTypeManager) {
		super(nodeTypeManager);
	}

	public String getNodeTypeName() {
		return "nt:unstructured";
	}

	protected void createChildren(Node node) throws RepositoryException {
		// no children to create
	}

	public void storeContent(Session session, Node node, UMOMessage message)
			throws RepositoryException, IOException {

		node.setProperty(NtResourceHandler.JCR_DATA_PROPERTY_NAME, message
				.getPayload().toString());

		Object payload = message.getPayload();

		if (payload instanceof Map) {
			JcrMessageUtils.storeProperties(session, node, (Map) payload);

		} else if (payload instanceof Collection) {
			node.setProperty(NtResourceHandler.JCR_DATA_PROPERTY_NAME,
					JcrMessageUtils.newPropertyValues(session,
							(Collection) payload));
		} else {
			node.setProperty(NtResourceHandler.JCR_DATA_PROPERTY_NAME,
					JcrMessageUtils.newPropertyValue(session, payload));
		}

	}
}
