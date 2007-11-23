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
 * Defines a handler that extracts the necessary information from a
 * <code>UMOMessage</code> in order to store content in a JCR container while
 * trying to comply to the constraint of the desired node type.
 * 
 * @author David Dossot (david@dossot.net)
 */
public interface NodeTypeHandler {

	String getNodeTypeName();

	Node newNode(Session session, Node targetNode, String nodeRelPath,
			UMOMessage message) throws RepositoryException, IOException;

}
