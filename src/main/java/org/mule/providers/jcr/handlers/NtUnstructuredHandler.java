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

import org.mule.providers.jcr.JcrUtils;
import org.mule.umo.UMOMessage;

/**
 * A handler for nt:unstructured types of nodes.
 * 
 * @author David Dossot (david@dossot.net)
 */
final class NtUnstructuredHandler extends AbstractNodeTypeHandler {

	public String getNodeTypeName() {
		return "nt:unstructured";
	}

	protected void createChildren(Node node) throws RepositoryException {
		// no children to create
	}

	public void updateContent(Session session, Node node, UMOMessage message)
			throws RepositoryException, IOException {

		node.setProperty(NtResourceHandler.JCR_DATA_PROPERTY_NAME, message
				.getPayload().toString());

		Object payload = message.getPayload();

		if (payload instanceof Map) {
			JcrUtils.storeProperties(session, node, (Map) payload);

		} else if (payload instanceof Collection) {
			node.setProperty(NtResourceHandler.JCR_DATA_PROPERTY_NAME,
					JcrUtils.newPropertyValues(session,
							(Collection) payload));
		} else {
			node.setProperty(NtResourceHandler.JCR_DATA_PROPERTY_NAME,
					JcrUtils.newPropertyValue(session, payload));
		}

	}
}
