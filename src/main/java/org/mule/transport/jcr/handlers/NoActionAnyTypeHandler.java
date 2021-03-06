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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleMessage;
import org.mule.transport.NullPayload;

/**
 * A type handler that becomes assigned to any type and performs no action.
 * 
 * @author David Dossot (david@dossot.net)
 */
final class NoActionAnyTypeHandler extends AbstractNodeTypeHandler {

	private transient final Log logger = LogFactory.getLog(getClass());

	private final String nodeTypeName;

	public NoActionAnyTypeHandler(final String nodeTypeName) {

		this.nodeTypeName = nodeTypeName;
	}

	public String getNodeTypeName() {
		return nodeTypeName;
	}

	@Override
	protected void createChildren(final Node node) throws RepositoryException {
		// no children to create
	}

	public void updateContent(final Session session, final Node node,
			MuleMessage message) throws RepositoryException {
		// do not store anything

		if ((message != null) && (message.getPayload() != null)
				&& (!(message.getPayload() instanceof NullPayload))) {
			logger
					.warn("No action node type handler used for type '"
							+ getNodeTypeName()
							+ "': message will not be persisted. "
							+ "Provide your own type handler if this behavior is not correct.");
		}
	}
}
