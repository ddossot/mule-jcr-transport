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
import java.util.Collection;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.api.MuleMessage;
import org.mule.transport.jcr.JcrUtils;

/**
 * A handler for nt:unstructured types of nodes.
 * 
 * @author David Dossot (david@dossot.net)
 */
final class NtUnstructuredHandler extends AbstractNodeTypeHandler {

    public String getNodeTypeName() {
        return "nt:unstructured";
    }

    @Override
    protected void createChildren(final Node node) throws RepositoryException {
        // no children to create
    }

    public void updateContent(final Session session, final Node node,
            final MuleMessage message) throws RepositoryException, IOException {

        node.setProperty(NtResourceHandler.JCR_DATA_PROPERTY_NAME, message
                .getPayload().toString());

        final Object payload = message.getPayload();

        if (payload instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, ?> mapPayload = (Map<String, ?>) payload;
            JcrUtils.storeProperties(session, node, mapPayload);

        } else if (payload instanceof Collection) {
            node.setProperty(NtResourceHandler.JCR_DATA_PROPERTY_NAME, JcrUtils
                    .newPropertyValues(session, (Collection<?>) payload));
        } else {
            node.setProperty(NtResourceHandler.JCR_DATA_PROPERTY_NAME, JcrUtils
                    .newPropertyValue(session, payload));
        }

    }
}
