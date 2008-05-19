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
import javax.jcr.nodetype.NodeType;

import org.mule.util.StringUtils;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * The manager for all JCR node type handlers.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class NodeTypeHandlerManager {

    private final ConcurrentHashMap registeredHandlers;

    public NodeTypeHandlerManager() {
        registeredHandlers = new ConcurrentHashMap();
        registerHandler(new NtFileHandler());
        registerHandler(new NtLinkedFileHandler());
        registerHandler(new NtResourceHandler());
        registerHandler(new NtUnstructuredHandler());
    }

    /**
     * Registers a new <code>NodeTypeHandler</code>. The manager calls
     * initialize first, then registers the handler.
     * 
     * @param handler
     *            the new handler.
     */
    public void registerHandler(NodeTypeHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException(
                    "A new type handler can not be null!");
        }

        handler.initialize(this);
        
        registeredHandlers.put(handler.getNodeTypeName(), handler);
    }

    /**
     * Get the default type handler for child nodes of the passed parent node.
     * 
     * @param parentNode
     *            the node whose default child node type will be used to get an
     *            handler.
     * 
     * @return the default child type handler.
     */
    public NodeTypeHandler getChildNodeTypeHandler(Node parentNode) {
        if (parentNode == null) {
            throw new IllegalArgumentException("A parent node can not be null!");
        }

        try {
            NodeType defaultPrimaryType =
                    parentNode.getDefinition().getDefaultPrimaryType();

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

    /**
     * Gets the node type handler for a particular node.
     * 
     * @param node
     *            the node for which the appropriate type handler will be
     *            returned.
     * 
     * @return the node type handler.
     */
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

    /**
     * Gets the node type handler for a particular node type name.
     * 
     * @param nodeTypeName
     *            the node type name for which the appropriate type handler will
     *            be returned.
     * 
     * @return the node type handler.
     */
    public NodeTypeHandler getNodeTypeHandler(String nodeTypeName) {

        if (StringUtils.isEmpty(nodeTypeName)) {
            throw new IllegalArgumentException(
                    "The node type name can not be empty!");
        }

        NodeTypeHandler handler =
                (NodeTypeHandler) registeredHandlers.get(nodeTypeName);

        if (handler == null) {
            handler = new NoActionAnyTypeHandler(nodeTypeName);
            registeredHandlers.putIfAbsent(nodeTypeName, handler);
        }

        return handler;
    }

}
