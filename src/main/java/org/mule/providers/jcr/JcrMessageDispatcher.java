/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.impl.MuleMessage;
import org.mule.impl.RequestContext;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.jcr.filters.AbstractJcrNameFilter;
import org.mule.providers.jcr.filters.JcrNodeNameFilter;
import org.mule.providers.jcr.filters.JcrPropertyNameFilter;
import org.mule.providers.jcr.handlers.NodeTypeHandler;
import org.mule.providers.jcr.i18n.JcrMessages;
import org.mule.routing.filters.logic.AndFilter;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.util.StringUtils;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicReference;

/**
 * A dispatcher for reading and writing in a JCR container.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcher extends AbstractMessageDispatcher {
    private final JcrConnector jcrConnector;

    private final AtomicReference nodeNamePatternFilterRef =
            new AtomicReference();

    private final AtomicReference propertyNamePatternFilterRef =
            new AtomicReference();

    private Session dispatcherSession;

    public JcrMessageDispatcher(UMOImmutableEndpoint endpoint) {
        super(endpoint);
        jcrConnector = (JcrConnector) endpoint.getConnector();
    }

    public void doConnect() throws Exception {
        // check the endpoint URI points to an existing node (even if this node
        // can be removed later, it is good to fail fast if it does not exist at
        // start time)
        String endpointURI = endpoint.getEndpointURI().getAddress();

        dispatcherSession = jcrConnector.newSession();

        if (dispatcherSession.itemExists(endpointURI)) {
            new IllegalStateException("The dispatcher endpoint URI ("
                + endpointURI
                    + ") does not point to an existing item!");
        }

        refreshEndpointFilter();
    }

    public void doDisconnect() throws Exception {
        jcrConnector.terminateSession(dispatcherSession);
        dispatcherSession = null;
    }

    public void doDispose() {
        // NOOP
    }

    public synchronized void refreshEndpointFilter() {
        nodeNamePatternFilterRef.set(getPropertyNamePatternFilter(
                getEndpoint().getFilter(), JcrNodeNameFilter.class));

        propertyNamePatternFilterRef.set(getPropertyNamePatternFilter(
                getEndpoint().getFilter(), JcrPropertyNameFilter.class));
    }

    public Session getSession() {
        return jcrConnector.validateSession(dispatcherSession);
    }

    /**
     * @see org.mule.providers.jcr.JcrMessageDispatcher#doSend(UMOEvent) doSend
     */
    public void doDispatch(UMOEvent event) throws Exception {
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
     * property (<code>JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE</code>),
     * the target item where content will be stored will determined by
     * navigating from the endpoint URI item to the item defined by optional
     * event properties (JcrConnector.JCR_NODE_RELPATH_PROPERTY and
     * JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY). If none of these properties
     * is defined, the item referred to by the endpoint URI will be used.Note
     * that if a node UUID is specified in a property, it will supersede the
     * endpoint URI and the transport will navigate from the node referred to by
     * its unique identifier.
     * </p>
     * 
     * <p>
     * If an existing target item is found and is a node, the appropriate
     * {@link org.mule.providers.jcr.handlers.NodeTypeHandler NodeTypeHandler}
     * will be used to convert the <code>UMOMessage</code> payload into valid
     * JCR content (nodes and properties).
     * </p>
     * 
     * <p>
     * If an existing target item is found and is a property, the
     * <code>UMOMessage</code> payload will be directly written to it, using a
     * simple conversion mechanism. Note that if the payload is a
     * <code>Collection</code>, the property will be multi-valued.
     * </p>
     * 
     * <p>
     * If no existing target item is found or if the creation of a new node is
     * forced (see first paragraph), a new node will be created, under the
     * absolute path defined by the endpoint URI, with a content extracted from
     * the <code>UMOMessage</code> payload and stored according to the type
     * defined in the event or connector property (<code>JcrConnector.JCR_NODE_TYPE_NAME</code>).
     * If the endpoint URI points to a property and not a node, an exception
     * will be raised.
     * </p>
     * 
     * @see org.mule.providers.jcr.JcrConnector Property names constants
     * 
     * @return the source <code>UMOMessage</code>.
     */
    public UMOMessage doSend(UMOEvent event) throws Exception {
        boolean alwaysCreate =
                Boolean.valueOf(
                        (String) event.getProperty(
                                JcrConnector.JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY,
                                true)).booleanValue();

        String nodeUUID = getNodeUUID(event);
        String nodeRelPath = getNodeRelPath(event);
        String propertyRelPath = getPropertyRelPath(event);
        Session session = getSession();

        Item targetItem =
                alwaysCreate ? null : getTargetItem(session, nodeUUID,
                        nodeRelPath, propertyRelPath);

        if (targetItem != null) {
            // write payload to node or property
            Object payload = event.getTransformedMessage();

            if (logger.isDebugEnabled()) {
                logger.debug("Writing '"
                    + payload
                        + "' to item: "
                        + targetItem.getPath());
            }

            if (targetItem.isNode()) {
                Node targetNode = (Node) targetItem;

                jcrConnector.getNodeTypeHandlerManager().getNodeTypeHandler(
                        targetNode).updateContent(session, targetNode,
                        event.getMessage());
            } else {
                Property targetProperty = (Property) targetItem;

                if ((payload instanceof Collection)) {
                    targetProperty.setValue(JcrUtils.newPropertyValues(session,
                            (Collection) payload));
                } else {
                    targetProperty.setValue(JcrUtils.newPropertyValue(session,
                            payload));
                }
            }

        } else {
            targetItem = getTargetItem(session, nodeUUID);

            if (targetItem == null) {
                throw new DispatchException(
                        JcrMessages.noNodeFor("Endpoint URI: "
                            + endpoint.getEndpointURI().toString()
                                + " ; NodeUUDI: "
                                + nodeUUID), event.getMessage(), endpoint);

            } else if (targetItem.isNode()) {
                Node targetParentNode = (Node) targetItem;

                // create the target node, based on its type and relpath
                String nodeTypeName =
                        (String) event.getProperty(
                                JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, true);

                NodeTypeHandler nodeTypeHandler;

                if (StringUtils.isNotBlank(nodeTypeName)) {
                    nodeTypeHandler =
                            jcrConnector.getNodeTypeHandlerManager().getNodeTypeHandler(
                                    nodeTypeName);
                } else {
                    nodeTypeHandler =
                            jcrConnector.getNodeTypeHandlerManager().getChildNodeTypeHandler(
                                    targetParentNode);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Selected node type handler: "
                        + nodeTypeHandler
                            + " for node: "
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

    /**
     * <p>
     * Receives JCR content from the configured endpoint, using optional event
     * properties to define the target repository item. Unless an exception is
     * thrown, a <code>UMOMessage</code> will always be retrieved, possibly
     * with a null payload if no content was acessible.
     * </p>
     * 
     * <p>
     * The content is extracted from the property or properties that were
     * targeted by the endpoint path, filters and event optional parameters.
     * </p>
     * 
     * <p>
     * The first step of the content fetching consists in selecting a target
     * item from the repository. This item is selected by using the path of the
     * endpoint and by appending any optional relative paths that could have
     * been specified as event properties for the current <code>UMOEvent</code> (<code>JcrConnector.JCR_NODE_RELPATH_PROPERTY</code>
     * and <code>JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY</code>).
     * Alternatively, an event property (<code>JcrConnector.JCR_NODE_UUID_PROPERTY</code>)
     * can be used to specify the UUID to use to select the target item: if this
     * is done, the endpoint URI will be ignored. The relative node and property
     * paths, if present, will be applied to the node selected by its UUID, to
     * further navigate the target item.
     * </p>
     * 
     * <p>
     * The second step consists in applying any <code>JcrNodeNameFilter</code>
     * or <code>JcrPropertyNameFilter</code> that could have been defined on
     * the endpoint to further narrow the target item from which content will be
     * extracted. If more than one node is selected, the first one will be
     * arbitrarily used as the target item and a warning will be issued. If no
     * item can be selected, a null payload will be used for the returned
     * <code>UMOMessage</code>.
     * </p>
     * 
     * <p>
     * The final step is the content extraction that will be used as the
     * <code>UMOMessage</code> payload. For this, the following rules apply,
     * depending on the target item:
     * <ul>
     * <li>For a single-valued property, the payload will be the property
     * value.</li>
     * <li>For a multi-valued property, the payload will be a <code>List</code>
     * of values.</li>
     * <li>For a node, the payload will be a <code>Map</code> of property
     * names and property values (for these values, the previous two rules will
     * apply).</li>
     * </ul>
     * </p>
     * 
     * @see org.mule.providers.jcr.JcrConnector Property names constants
     * 
     * @param ignoredTimeout
     *            ignored timeout parameter.
     * 
     * @return the message fetched from this dispatcher.
     */
    public UMOMessage doReceive(long ignoredTimeout) throws Exception {
        UMOEvent event = RequestContext.getEvent();

        if (logger.isDebugEnabled()) {
            logger.debug("Receiving from JCR with event: "
                + event);
        }

        Item targetItem =
                getTargetItem(getSession(), getNodeUUID(event),
                        getNodeRelPath(event), getPropertyRelPath(event));

        Object rawJcrContent = null;

        if (targetItem != null) {
            if (targetItem.isNode()) {
                rawJcrContent =
                        getRawContentFromNode(rawJcrContent, targetItem);
            } else {
                rawJcrContent = getRawContentFromProperty(targetItem);
            }
        }

        Object transformedContent =
                rawJcrContent == null ? null
                        : jcrConnector.getDefaultResponseTransformer().transform(
                                rawJcrContent);

        UMOMessageAdapter messageAdapter;

        if ((transformedContent != null)
            && (getEndpoint().isStreaming())) {

            if (transformedContent instanceof InputStream) {
                messageAdapter =
                        jcrConnector.getStreamMessageAdapter(
                                (InputStream) transformedContent, null);
            } else {
                messageAdapter =
                        jcrConnector.getStreamMessageAdapter(
                                new ByteArrayInputStream(
                                        jcrConnector.getMessageAdapter(
                                                transformedContent).getPayloadAsBytes()),
                                null);
            }
        } else {
            messageAdapter = jcrConnector.getMessageAdapter(transformedContent);
        }

        return new MuleMessage(messageAdapter);
    }

    private String getNodeUUID(UMOEvent event) {
        return event != null ? (String) event.getProperty(
                JcrConnector.JCR_NODE_UUID_PROPERTY, true) : null;
    }

    private String getPropertyRelPath(UMOEvent event) {
        return event != null ? JcrUtils.getParsableEventProperty(event,
                JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY) : null;
    }

    private String getNodeRelPath(UMOEvent event) {
        return event != null ? JcrUtils.getParsableEventProperty(event,
                JcrConnector.JCR_NODE_RELPATH_PROPERTY) : null;
    }

    private Object getRawContentFromProperty(Item targetItem)
            throws RepositoryException {

        if (logger.isDebugEnabled()) {
            logger.debug("Getting payload for property: "
                + targetItem.getPath());
        }

        return targetItem;
    }

    private Object getRawContentFromNode(Object rawJcrContent, Item targetItem)
            throws RepositoryException {

        String nodeNamePatternFilter = (String) nodeNamePatternFilterRef.get();

        if (nodeNamePatternFilter != null) {
            targetItem =
                    getTargetItemByNodeNamePatternFilter(targetItem,
                            nodeNamePatternFilter);
        }

        String propertyNamePatternFilter =
                (String) propertyNamePatternFilterRef.get();

        if (targetItem != null) {
            if (propertyNamePatternFilter != null) {

                if (logger.isDebugEnabled()) {
                    logger.debug("Applying property name pattern filter: "
                        + propertyNamePatternFilter);
                }

                PropertyIterator properties =
                        ((Node) targetItem).getProperties(propertyNamePatternFilter);

                // if the map contains only one property, because we
                // have applied a filter, we assume the intention was to
                // get a single property value
                if (properties.getSize() == 0) {
                    logger.warn(JcrMessages.noNodeFor(targetItem.getPath()
                        + "["
                            + propertyNamePatternFilter
                            + "]").getMessage());
                    rawJcrContent = null;
                } else if (properties.getSize() == 1) {
                    rawJcrContent = properties.next();
                } else {
                    rawJcrContent = properties;
                }

            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Getting payload for node: "
                        + targetItem.getPath());
                }

                // targetItem is a node
                rawJcrContent = targetItem;
            }
        }
        return rawJcrContent;
    }

    private Item getTargetItem(Session session, String nodeUUID)
            throws RepositoryException, PathNotFoundException {
        return getTargetItem(session, nodeUUID, null, null);
    }

    private Item getTargetItem(Session session, String nodeUUID,
            String nodeRelpath, String propertyRelPath)
            throws RepositoryException, PathNotFoundException {

        Item targetItem = null;

        if (nodeUUID != null) {
            targetItem =
                    getTargetItemFromUUID(session, nodeUUID, nodeRelpath,
                            propertyRelPath);
        } else {
            targetItem =
                    getTargetItemFromPath(session, nodeRelpath, propertyRelPath);
        }

        return targetItem;
    }

    private Item getTargetItemByNodeNamePatternFilter(Item targetItem,
            String nodeNamePatternFilter) throws RepositoryException {

        if (logger.isDebugEnabled()) {
            logger.debug("Applying node name pattern filter: "
                + nodeNamePatternFilter);
        }

        NodeIterator nodes =
                ((Node) targetItem).getNodes(nodeNamePatternFilter);

        if (nodes.getSize() == 0) {
            logger.warn(JcrMessages.noNodeFor(targetItem.getPath()
                + "["
                    + nodeNamePatternFilter
                    + "]").getMessage());

            targetItem = null;

        } else {
            if (nodes.getSize() > 1) {
                logger.warn(JcrMessages.moreThanOneNodeFor(targetItem.getPath()
                    + "["
                        + nodeNamePatternFilter
                        + "]").getMessage());
            }

            targetItem = nodes.nextNode();
        }
        return targetItem;
    }

    private Item getTargetItemFromPath(Session session, String nodeRelpath,
            String propertyRelPath) throws RepositoryException,
            PathNotFoundException {

        if (logger.isDebugEnabled()) {
            logger.debug("Accessing JCR container for endpoint: "
                + getEndpoint());
        }

        final TargetItem targetItem =
                new TargetItem(null, endpoint.getEndpointURI().getAddress());

        if (session.itemExists(targetItem.getAbsolutePath())) {
            targetItem.setItem(session.getItem(targetItem.getAbsolutePath()));

            getRelativeTargetItem(targetItem, nodeRelpath, propertyRelPath);
        }

        if ((logger.isDebugEnabled())
            && (targetItem.getItem() == null)) {
            logger.debug(JcrMessages.noNodeFor(targetItem.getAbsolutePath()).getMessage());
        }

        return targetItem.getItem();
    }

    private Item getTargetItemFromUUID(Session session, String nodeUUID,
            String nodeRelpath, String propertyRelPath) {
        try {
            final Node nodeByUUID = session.getNodeByUUID(nodeUUID);

            if (nodeByUUID != null) {
                TargetItem targetItem = new TargetItem(nodeByUUID, nodeUUID);
                getRelativeTargetItem(targetItem, nodeRelpath, propertyRelPath);

                if ((logger.isDebugEnabled())
                    && (targetItem.getItem() == null)) {
                    logger.debug(JcrMessages.noNodeFor(
                            targetItem.getAbsolutePath()).getMessage());
                }

                return targetItem.getItem();
            }

        } catch (RepositoryException re) {
            logger.warn(JcrMessages.noNodeFor("UUID="
                + nodeUUID).getMessage());
        }

        return null;
    }

    private void getRelativeTargetItem(final TargetItem targetItem,
            String nodeRelpath, String propertyRelPath)
            throws RepositoryException, PathNotFoundException {

        if (nodeRelpath != null) {
            Node node = targetItem.asNodeOrNull();

            if (node != null) {
                targetItem.setAbsolutePath(targetItem.getAbsolutePath()
                    + "/"
                        + nodeRelpath);

                if (node.hasNode(nodeRelpath)) {
                    targetItem.setItem(node.getNode(nodeRelpath));
                } else {
                    targetItem.setItem(null);
                }
            } else {
                throw new IllegalArgumentException("The node relative path "
                    + nodeRelpath
                        + " has been specified though the target item path "
                        + targetItem.getAbsolutePath()
                        + " did not refer to a node!");
            }
        }

        if (propertyRelPath != null) {
            Node node = targetItem.asNodeOrNull();

            if (node != null) {
                targetItem.setAbsolutePath(targetItem.getAbsolutePath()
                    + "/"
                        + propertyRelPath);

                if (node.hasProperty(propertyRelPath)) {
                    targetItem.setItem(node.getProperty(propertyRelPath));
                } else {
                    targetItem.setItem(null);
                }
            } else {
                throw new IllegalArgumentException(
                        "The property relative path "
                            + propertyRelPath
                                + " has been specified though the target item path "
                                + targetItem.getAbsolutePath()
                                + " did not refer to a node!");
            }
        }
    }

    private static String getPropertyNamePatternFilter(UMOFilter filter,
            Class filterClass) {

        String pattern = null;

        if (filter != null) {
            if (filter instanceof AbstractJcrNameFilter) {
                if (filter.getClass().equals(filterClass)) {
                    pattern = ((AbstractJcrNameFilter) filter).getPattern();
                }
            } else if (filter instanceof AndFilter) {
                pattern =
                        getPropertyNamePatternFilter(
                                ((AndFilter) filter).getLeftFilter(),
                                filterClass);

                if (pattern == null) {
                    pattern =
                            getPropertyNamePatternFilter(
                                    ((AndFilter) filter).getRightFilter(),
                                    filterClass);
                }
            } else {
                throw new IllegalArgumentException(JcrMessages.badFilterType(
                        filter.getClass()).getMessage());
            }
        }

        return pattern;
    }

    private static class TargetItem {
        private Item item;

        private String absolutePath;

        public TargetItem(Item item, String absolutePath) {
            this.item = item;
            this.absolutePath = absolutePath;
        }

        public Node asNodeOrNull() {
            return item.isNode() ? (Node) item : null;
        }

        public Item getItem() {
            return item;
        }

        public void setItem(Item item) {
            this.item = item;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        public void setAbsolutePath(String absolutePath) {
            this.absolutePath = absolutePath;
        }
    }

}
