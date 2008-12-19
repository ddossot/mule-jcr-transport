/*
 * $Id: MessageDispatcher.vm 10961 2008-02-22 19:01:02Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import java.util.Collection;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.DispatchException;
import org.mule.transport.AbstractMessageDispatcher;
import org.mule.transport.jcr.handlers.NodeTypeHandler;
import org.mule.transport.jcr.i18n.JcrMessages;
import org.mule.transport.jcr.support.JcrPropertyUtils;
import org.mule.transport.jcr.support.JcrUtils;
import org.mule.util.StringUtils;

/**
 * A dispatcher for writing to a JCR container.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcher extends AbstractMessageDispatcher {

    private final JcrConnector jcrConnector;

    private Session dispatcherSession;

    public Session getSession() {
        dispatcherSession = jcrConnector.validateSession(dispatcherSession);
        return dispatcherSession;
    }

    public JcrMessageDispatcher(final OutboundEndpoint endpoint) {
        super(endpoint);
        jcrConnector = (JcrConnector) endpoint.getConnector();
    }

    @Override
    public void doConnect() throws Exception {
        dispatcherSession = jcrConnector.newSession();
    }

    @Override
    public void doDisconnect() throws Exception {
        jcrConnector.terminateSession(dispatcherSession);
    }

    @Override
    public void doDispose() {
        dispatcherSession = null;
    }

    /**
     * @see org.mule.transport.jcr.JcrMessageDispatcher#doSend(UMOEvent) doSend
     */
    @Override
    public void doDispatch(final MuleEvent event) throws Exception {
        doSend(event);
    }

    /**
     * <p>
     * Sends content to the configured JCR endpoint, using optional event
     * properties to define the target repository item and the node type name to
     * use.
     * </p>
     * 
     * <p>
     * Unless the creation of child nodes is forced by an event or endpoint
     * property (<code>JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE</code>), the
     * target item where content will be stored will determined the same way as
     * explained in the <code>doReceive</code> method.
     * </p>
     * 
     * <p>
     * If an existing target item is found and is a node, the appropriate
     * {@link org.mule.transport.jcr.handlers.NodeTypeHandler NodeTypeHandler}
     * will be used to convert the <code>MuleMessage</code> payload into valid
     * JCR content (nodes and properties).
     * </p>
     * 
     * <p>
     * If an existing target item is found and is a property, the
     * <code>MuleMessage</code> payload will be directly written to it, using a
     * simple conversion mechanism. Note that if the payload is a
     * <code>Collection</code>, the property will be multi-valued.
     * </p>
     * 
     * <p>
     * If no existing target item is found or if the creation of a new node is
     * forced (see first paragraph), a new node will be created, under the
     * absolute path defined by the endpoint URI, with a content extracted from
     * the <code>MuleMessage</code> payload and stored according to the type
     * defined in the event or connector property (
     * <code>JcrConnector.JCR_NODE_TYPE_NAME</code>). If the endpoint URI points
     * to a property and not a node, an exception will be raised.
     * </p>
     * 
     * @see org.mule.transport.jcr.JcrConnector Property names constants
     * 
     * @return the source <code>MuleMessage</code>.
     */
    @Override
    public MuleMessage doSend(final MuleEvent event) throws Exception {
        final boolean alwaysCreate = Boolean.valueOf(
                (String) event.getProperty(
                        JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY,
                        true)).booleanValue();

        final String nodeUUID = JcrUtils.getNodeUUID(event);
        final String nodeRelPath = JcrUtils.getNodeRelPath(event);
        final String propertyRelPath = JcrPropertyUtils.getPropertyRelPath(event);
        final Session session = getSession();

        Item targetItem = alwaysCreate ? null : JcrUtils.getTargetItem(session,
                endpoint, event, true);

        if (targetItem != null) {
            // write payload to node or property
            final Object payload = event.transformMessage();

            if (logger.isDebugEnabled()) {
                logger.debug("Writing '" + payload + "' to item: "
                        + targetItem.getPath());
            }

            if (targetItem.isNode()) {
                final Node targetNode = (Node) targetItem;

                jcrConnector.getNodeTypeHandlerManager().getNodeTypeHandler(
                        targetNode).updateContent(session, targetNode,
                        event.getMessage());
            } else {
                final Property targetProperty = (Property) targetItem;

                if ((payload instanceof Collection)) {
                    targetProperty.setValue(JcrPropertyUtils.newPropertyValues(session,
                            (Collection<?>) payload));
                } else {
                    targetProperty.setValue(JcrPropertyUtils.newPropertyValue(session,
                            payload));
                }
            }

        } else {
            targetItem = JcrUtils
                    .getTargetItem(session, endpoint, event, false);

            if (targetItem == null) {
                throw new DispatchException(JcrMessages
                        .noNodeFor("Endpoint URI: "
                                + endpoint.getEndpointURI().toString()
                                + " ; NodeUUID: " + nodeUUID), event
                        .getMessage(), endpoint);

            } else if (targetItem.isNode()) {
                final Node targetParentNode = (Node) targetItem;

                // create the target node, based on its type and relpath
                final String nodeTypeName = JcrUtils.getNodeTypeName(event);

                NodeTypeHandler nodeTypeHandler;

                if (StringUtils.isNotBlank(nodeTypeName)) {
                    nodeTypeHandler = jcrConnector.getNodeTypeHandlerManager()
                            .getNodeTypeHandler(nodeTypeName);
                } else {
                    nodeTypeHandler = jcrConnector.getNodeTypeHandlerManager()
                            .getChildNodeTypeHandler(targetParentNode);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Selected node type handler: "
                            + nodeTypeHandler + " for node: "
                            + targetParentNode.getPath());
                }

                nodeTypeHandler.createNode(session, targetParentNode,
                        nodeRelPath, event.getMessage());
            } else {
                throw new IllegalArgumentException(
                        "The provided nodeRelPath ("
                                + nodeRelPath
                                + ") and propertyRelPath ("
                                + propertyRelPath
                                + ") point to a missing item, hence the connector tries to create a new node "
                                + "but the endpoint URI, used as a parent node, refers to a JCR property.");
            }

        }

        session.save();

        return event.getMessage();
    }

}
