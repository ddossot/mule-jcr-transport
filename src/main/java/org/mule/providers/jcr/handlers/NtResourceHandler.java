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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.umo.UMOMessage;

/**
 * A handler for nt:resource types of nodes.
 * 
 * @author David Dossot (david@dossot.net)
 */
final class NtResourceHandler extends AbstractNodeTypeHandler {

	static final String NT_RESOURCE_NODE_TYPE = "nt:resource";

	static final String JCR_LAST_MODIFIED_PROPERTY_NAME = "jcr:lastModified";

	static final String JCR_DATA_PROPERTY_NAME = "jcr:data";

	static final String JCR_MIME_TYPE_PROPERTY_NAME = "jcr:mimeType";

	static final String JCR_ENCODING_PROPERTY_NAME = "jcr:encoding";

	public String getNodeTypeName() {
		return NT_RESOURCE_NODE_TYPE;
	}

	protected void createChildren(Node node) throws RepositoryException {
		// no children to create
	}

	public void updateContent(Session session, Node node, UMOMessage message)
			throws RepositoryException, IOException {

		Object payload = message.getPayload();

		String mimeType = null;
		String encoding = null;
		Object data = null;
		Calendar lastModified = null;

		if (payload instanceof Map) {
			// if the payload is a Map, we assume that the intention is to pass
			// a map of property names and values
			Map mapPayload = (Map) payload;

			encoding = (String) mapPayload.get(JCR_ENCODING_PROPERTY_NAME);
			mimeType = (String) mapPayload.get(JCR_MIME_TYPE_PROPERTY_NAME);
			data = mapPayload.get(JCR_DATA_PROPERTY_NAME);
			lastModified = (Calendar) mapPayload
					.get(JCR_LAST_MODIFIED_PROPERTY_NAME);
		} else {
			// look into message props for meta and payload for data
			encoding = (String) message.getProperty(JCR_ENCODING_PROPERTY_NAME);
			mimeType = (String) message
					.getProperty(JCR_MIME_TYPE_PROPERTY_NAME);
			data = payload;
			lastModified = (Calendar) message
					.getProperty(JCR_LAST_MODIFIED_PROPERTY_NAME);
		}

		if (mimeType == null) {
			throw new IllegalArgumentException("Property: "
					+ JCR_MIME_TYPE_PROPERTY_NAME
					+ " can not be null for node type: " + getNodeTypeName());
		}

		node.setProperty(JCR_MIME_TYPE_PROPERTY_NAME, mimeType);

		InputStream binaryContent;

		if (data instanceof InputStream) {
			binaryContent = (InputStream) data;
		} else {
			try {
				binaryContent = new ByteArrayInputStream(message
						.getPayloadAsBytes());
			} catch (Exception e) {
				throw new IOException(
						"Can not extract binary content from UMOMessage: "
								+ message + ". Exception is: " + e);
			}
		}

		node.setProperty(JCR_DATA_PROPERTY_NAME, binaryContent);

		// encoding is optional: do not set it, unless it has been specified as
		// a property
		if (encoding != null) {
			node.setProperty(JCR_ENCODING_PROPERTY_NAME, encoding);
		}

		// if no last modified is provided, a reasonable default is: now
		if (lastModified == null) {
			lastModified = Calendar.getInstance();
		}

		node.setProperty(JCR_LAST_MODIFIED_PROPERTY_NAME, lastModified);
	}
}
