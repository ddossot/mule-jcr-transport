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
 * A nt:file handler that uses nt:resource for the compulsory jcr:content child
 * node.
 * 
 * @author David Dossot (david@dossot.net)
 */
final class NtFileHandler extends AbstractNodeTypeHandler {

	static final String JCR_CONTENT_NODE_NAME = "jcr:content";

	public String getNodeTypeName() {
		return "nt:file";
	}

	@Override
	protected void createChildren(final Node node) throws RepositoryException {
		node.addNode(JCR_CONTENT_NODE_NAME,
				NtResourceHandler.NT_RESOURCE_NODE_TYPE);
	}

	public void updateContent(final Session session, final Node node,
			MuleMessage message) throws RepositoryException, IOException {

		final Node contentNode = node.getNode(JCR_CONTENT_NODE_NAME);

		getNodeTypeManager().getNodeTypeHandler(
				NtResourceHandler.NT_RESOURCE_NODE_TYPE).updateContent(session,
				contentNode, message);
	}
}
