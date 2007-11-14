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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.impl.MuleMessage;
import org.mule.impl.RequestContext;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.jcr.filters.AbstractJcrNameFilter;
import org.mule.providers.jcr.filters.JcrNodeNameFilter;
import org.mule.providers.jcr.filters.JcrPropertyNameFilter;
import org.mule.providers.jcr.i18n.JcrMessages;
import org.mule.routing.filters.logic.AndFilter;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;
import org.mule.util.StringUtils;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicReference;

/**
 * A dispatcher for reading and writing in a JCR container.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcher extends AbstractMessageDispatcher {
	private final JcrConnector jcrConnector;

	private AtomicReference nodeNamePatternFilterRef = new AtomicReference();

	private AtomicReference propertyNamePatternFilterRef = new AtomicReference();

	public JcrMessageDispatcher(UMOImmutableEndpoint endpoint) {
		super(endpoint);
		jcrConnector = (JcrConnector) endpoint.getConnector();
	}

	public void doConnect() throws Exception {
		refreshEndpointFilter();
	}

	public synchronized void refreshEndpointFilter() {
		nodeNamePatternFilterRef.set(getPropertyNamePatternFilter(getEndpoint()
				.getFilter(), JcrNodeNameFilter.class));

		propertyNamePatternFilterRef.set(getPropertyNamePatternFilter(
				getEndpoint().getFilter(), JcrPropertyNameFilter.class));
	}

	public void doDisconnect() throws Exception {
		// NOOP
	}

	public void doDispose() {
		// NOOP
	}

	public void doDispatch(UMOEvent event) throws Exception {
		doSend(event);
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		/*
		 * IMPLEMENTATION NOTE: Should send the event payload over the
		 * transport. If there is a response from the transport it shuold be
		 * returned from this method. The sendEvent method is called when the
		 * endpoint is running synchronously and any response returned will
		 * ultimately be passed back to the callee. This method is executed in
		 * the same thread as the request thread.
		 */
		throw new UnsupportedOperationException("doSend");
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
	 * is done, the endpoint URI will be ignored.
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
	 * @param ignoredTimeout
	 *            ignored timeout parameter.
	 * 
	 * @return the message fetched from this dispatcher.
	 */
	public UMOMessage doReceive(long ignoredTimeout) throws Exception {
		Session session = jcrConnector.getSession();

		Object rawJcrContent = null;
		Item targetItem = null;
		UMOEvent event = RequestContext.getEvent();
		String nodeUUID = null;
		String nodeRelpath = "";
		String propertyRelPath = "";

		if (event != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Receiving from JCR with event: " + event);
			}

			nodeUUID = (String) event.getProperty(
					JcrConnector.JCR_NODE_UUID_PROPERTY, true);

			if (nodeUUID == null) {
				nodeRelpath = getNodeRelpathFromEvent(event);
				propertyRelPath = getPropertyRelpathFromEvent(event);
			}
		}

		if (nodeUUID != null) {
			targetItem = getTargetItemFromUUID(session, targetItem, nodeUUID);
		} else {
			targetItem = getTargetItemFromPath(session, targetItem,
					nodeRelpath, propertyRelPath);
		}

		if (targetItem != null) {
			if (targetItem.isNode()) {
				rawJcrContent = getRawContentFromNode(rawJcrContent, targetItem);
			} else {
				rawJcrContent = getRawContentFromProperty(targetItem);
			}
		}

		return new MuleMessage(jcrConnector
				.getMessageAdapter(rawJcrContent == null ? null : jcrConnector
						.getDefaultResponseTransformer().transform(
								rawJcrContent)));
	}

	// --- Private Methods ---

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
			targetItem = getTargetItemByNodeNamePatternFilter(targetItem,
					nodeNamePatternFilter);
		}

		String propertyNamePatternFilter = (String) propertyNamePatternFilterRef
				.get();

		if (targetItem != null) {
			if (propertyNamePatternFilter != null) {

				if (logger.isDebugEnabled()) {
					logger.debug("Applying property name pattern filter: "
							+ propertyNamePatternFilter);
				}

				PropertyIterator properties = ((Node) targetItem)
						.getProperties(propertyNamePatternFilter);

				// if the map contains only one property, because we
				// have applied a filter, we assume the intention was to
				// get a single property value
				if (properties.getSize() == 0) {
					logger.warn(JcrMessages
							.noNodeFor(propertyNamePatternFilter).getMessage());
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

	private Item getTargetItemByNodeNamePatternFilter(Item targetItem,
			String nodeNamePatternFilter) throws RepositoryException {

		if (logger.isDebugEnabled()) {
			logger.debug("Applying node name pattern filter: "
					+ nodeNamePatternFilter);
		}

		NodeIterator nodes = ((Node) targetItem)
				.getNodes(nodeNamePatternFilter);

		if (nodes.getSize() == 0) {
			logger.warn(JcrMessages.noNodeFor(nodeNamePatternFilter)
					.getMessage());

			targetItem = null;

		} else {
			if (nodes.getSize() > 1) {
				logger.warn(JcrMessages.moreThanOneNodeFor(
						nodeNamePatternFilter).getMessage());
			}

			targetItem = nodes.nextNode();
		}
		return targetItem;
	}

	private Item getTargetItemFromPath(Session session, Item targetItem,
			String nodeRelpath, String propertyRelPath)
			throws RepositoryException, PathNotFoundException {

		if (logger.isDebugEnabled()) {
			logger.debug("Receiving from JCR for endpoint: " + getEndpoint());
		}

		String itemAbsolutePath = endpoint.getEndpointURI().getAddress()
				+ nodeRelpath + propertyRelPath;

		if (session.itemExists(itemAbsolutePath)) {
			targetItem = session.getItem(itemAbsolutePath);
		} else {
			logger.warn(JcrMessages.noNodeFor(itemAbsolutePath).getMessage());
		}

		return targetItem;
	}

	private Item getTargetItemFromUUID(Session session, Item targetItem,
			String nodeUUID) {
		try {
			targetItem = session.getNodeByUUID(nodeUUID);
		} catch (RepositoryException re) {
			logger.warn(JcrMessages.noNodeFor("UUID=" + nodeUUID).getMessage());
		}
		return targetItem;
	}

	private String getPropertyRelpathFromEvent(UMOEvent event) {
		String propertyRelPath;
		propertyRelPath = (String) event.getProperty(
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY, true);

		if (StringUtils.isNotBlank(propertyRelPath)) {
			propertyRelPath = "/" + propertyRelPath;
		} else {
			propertyRelPath = "";
		}
		return propertyRelPath;
	}

	private String getNodeRelpathFromEvent(UMOEvent event) {
		String nodeRelpath;
		nodeRelpath = (String) event.getProperty(
				JcrConnector.JCR_NODE_RELPATH_PROPERTY, true);

		if (StringUtils.isNotBlank(nodeRelpath)) {
			nodeRelpath = "/" + nodeRelpath;
		} else {
			nodeRelpath = "";
		}
		return nodeRelpath;
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
				pattern = getPropertyNamePatternFilter(((AndFilter) filter)
						.getLeftFilter(), filterClass);

				if (pattern == null) {
					pattern = getPropertyNamePatternFilter(((AndFilter) filter)
							.getRightFilter(), filterClass);
				}
			} else {
				throw new IllegalArgumentException(JcrMessages.badFilterType(
						filter.getClass()).getMessage());
			}
		}

		return pattern;
	}
}
