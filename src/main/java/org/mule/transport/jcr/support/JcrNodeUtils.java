/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.jcr.support;

import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.observation.Event;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.transport.jcr.JcrConnector;
import org.mule.transport.jcr.JcrContentPayloadType;
import org.mule.transport.jcr.JcrMessage;
import org.mule.transport.jcr.i18n.JcrMessages;

/**
 * Utility class that provides methods for "detaching" JCR events and content
 * from the container so they can be used as payload that survives the closing
 * of the session.
 * 
 * @author David Dossot (david@dossot.net)
 */
public abstract class JcrNodeUtils {
	public static final String DEFAULT_DATE_FORMAT = "dd-MM-yy_HH-mm-ss.SSS";

	private static final Log LOG = LogFactory.getLog(JcrNodeUtils.class);

	private JcrNodeUtils() {
		throw new UnsupportedOperationException("Do not instantiate");
	}

	public static Object getItemPayload(final Item item)
			throws IllegalStateException, ValueFormatException,
			RepositoryException {

		if (item.isNode()) {
			return JcrPropertyUtils.getPropertiesPayload(((Node) item)
					.getProperties());
		}

		return JcrPropertyUtils.getPropertyPayload((Property) item);
	}

	public static String getNodeRelPath(final MuleEvent event) {
		return event != null ? JcrEventUtils.getParsableEventProperty(event,
				JcrConnector.JCR_NODE_RELPATH_PROPERTY) : null;
	}

	public static String getNodeTypeName(final MuleEvent event) {
		String nodeTypeName = null;

		final Object nodeTypeNameProperty = event.getProperty(
				JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY, true);

		if (nodeTypeNameProperty instanceof String) {
			nodeTypeName = (String) nodeTypeNameProperty;

		} else if (nodeTypeNameProperty instanceof List<?>) {
			@SuppressWarnings("unchecked")
			final List<String> nodeTypeNameProperties = (List<String>) nodeTypeNameProperty;

			if (nodeTypeNameProperties.size() > 0) {
				nodeTypeName = nodeTypeNameProperties.get(0);
			}

			if (nodeTypeNameProperties.size() > 1) {
				LOG.warn("Message property: "
						+ JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY
						+ " has multiple values, the connector will use: "
						+ nodeTypeName);
			}
		} else {
			LOG.warn("Message property: "
					+ JcrConnector.JCR_NODE_TYPE_NAME_PROPERTY
					+ " has an unusable value: " + nodeTypeNameProperty);
		}

		return nodeTypeName;
	}

	public static String getNodeUUID(final MuleEvent event) {
		return event != null ? (String) JcrEventUtils.getParsableEventProperty(
				event, JcrConnector.JCR_NODE_UUID_PROPERTY) : null;
	}

	private static QueryDefinition getQueryDefinition(final MuleEvent event) {
		return event != null ? new QueryDefinition((String) event
				.getProperty(JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY),
				JcrEventUtils.getParsableEventProperty(event,
						JcrConnector.JCR_QUERY_STATEMENT_PROPERTY)) : null;
	}

	public static Object getRawContentFromNode(Item targetItem,
			final String nodeNamePatternFilter,
			final String propertyNamePatternFilter) throws RepositoryException {

		Object result = null;

		if (nodeNamePatternFilter != null) {
			targetItem = getTargetItemByNodeNamePatternFilter(targetItem,
					nodeNamePatternFilter);
		}

		if (targetItem != null) {
			if (propertyNamePatternFilter != null) {

				if (LOG.isDebugEnabled()) {
					LOG.debug("Applying property name pattern filter: "
							+ propertyNamePatternFilter);
				}

				final PropertyIterator properties = ((Node) targetItem)
						.getProperties(propertyNamePatternFilter);

				// if the map contains only one property, because we
				// have applied a filter, we assume the intention was to
				// get a single property value
				if (properties.getSize() == 0) {
					LOG.warn(JcrMessages.noNodeFor(
							targetItem.getPath() + "["
									+ propertyNamePatternFilter + "]")
							.getMessage());
				} else if (properties.getSize() == 1) {
					result = properties.next();
				} else {
					result = properties;
				}

			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Getting payload for node: "
							+ targetItem.getPath());
				}

				// targetItem is a node
				result = targetItem;
			}
		}
		return result;
	}

	public static Object getRawContentFromProperty(final Item targetItem)
			throws RepositoryException {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Getting payload for property: " + targetItem.getPath());
		}

		return targetItem;
	}

	public static Item getTargetItem(final Session session,
			final ImmutableEndpoint endpoint, final MuleEvent event,
			final boolean navigateRelativePaths) throws RepositoryException,
			PathNotFoundException {

		final String nodeUUID = getNodeUUID(event);

		final QueryDefinition queryDefinition = getQueryDefinition(event);

		final String nodeRelPath = navigateRelativePaths ? getNodeRelPath(event)
				: null;

		final String propertyRelPath = navigateRelativePaths ? JcrPropertyUtils
				.getPropertyRelPath(event) : null;

		Item targetItem = null;

		if (LOG.isDebugEnabled()) {
			LOG.debug("Getting target item with nodeUUID='" + nodeUUID
					+ "', queryDefinition='" + queryDefinition
					+ "', nodeRelPath='" + nodeRelPath + "', propertyRelPath='"
					+ propertyRelPath + "'");
		}

		if (nodeUUID != null) {
			targetItem = getTargetItemFromUUID(session, nodeUUID, nodeRelPath,
					propertyRelPath);
		} else if ((queryDefinition != null)
				&& (queryDefinition.getStatement() != null)) {
			targetItem = getTargetItemFromQuery(session, queryDefinition,
					nodeRelPath, propertyRelPath);
		} else {
			targetItem = getTargetItemFromPath(session, endpoint, nodeRelPath,
					propertyRelPath);
		}

		return targetItem;
	}

	private static Item getTargetItemByNodeNamePatternFilter(
			final Item targetItem, final String nodeNamePatternFilter)
			throws RepositoryException {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Applying node name pattern filter: "
					+ nodeNamePatternFilter);
		}

		final NodeIterator nodes = ((Node) targetItem)
				.getNodes(nodeNamePatternFilter);

		return getTargetItemFromNodeIterator(targetItem.getPath() + "["
				+ nodeNamePatternFilter + "]", nodes);
	}

	private static Item getTargetItemFromNodeIterator(final String pathContext,
			final NodeIterator nodes) {

		if (nodes.getSize() == 0) {
			LOG.warn(JcrMessages.noNodeFor(pathContext).getMessage());

			return null;

		}

		if (nodes.getSize() > 1) {
			LOG.warn(JcrMessages.moreThanOneNodeFor(pathContext).getMessage());
		}

		return nodes.nextNode();
	}

	private static Item getTargetItemFromPath(final Session session,
			final ImmutableEndpoint endpoint, final String nodeRelpath,
			final String propertyRelPath) throws RepositoryException,
			PathNotFoundException {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Accessing JCR container for endpoint: " + endpoint);
		}

		final TargetItem targetItem = new TargetItem(null, endpoint
				.getEndpointURI().getAddress());

		if (session.itemExists(targetItem.getAbsolutePath())) {
			targetItem.setItem(session.getItem(targetItem.getAbsolutePath()));

			navigateToRelativeTargetItem(targetItem, nodeRelpath,
					propertyRelPath);
		}

		if ((LOG.isDebugEnabled()) && (targetItem.getItem() == null)) {
			LOG.debug(JcrMessages.noNodeFor(targetItem.getAbsolutePath())
					.getMessage());
		}

		return targetItem.getItem();
	}

	private static Item getTargetItemFromQuery(final Session session,
			final QueryDefinition queryDefinition, final String nodeRelpath,
			final String propertyRelPath) throws RepositoryException {

		final QueryResult queryResult = session.getWorkspace()
				.getQueryManager().createQuery(queryDefinition.getStatement(),
						queryDefinition.getLanguage()).execute();

		// there is no way to get a Property out of a QueryResult so we will
		// return only a Node
		final String context = queryDefinition.getLanguage() + ": "
				+ queryDefinition.getStatement();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Query : " + context + " returned: "
					+ queryResult.getRows().getSize() + " rows.");
		}

		final TargetItem targetItem = new TargetItem(
				getTargetItemFromNodeIterator(context, queryResult.getNodes()),
				context);

		navigateToRelativeTargetItem(targetItem, nodeRelpath, propertyRelPath);

		return targetItem.getItem();
	}

	private static Item getTargetItemFromUUID(final Session session,
			final String nodeUUID, final String nodeRelpath,
			final String propertyRelPath) {
		try {
			final Node nodeByUUID = session.getNodeByUUID(nodeUUID);

			if (nodeByUUID != null) {
				final TargetItem targetItem = new TargetItem(nodeByUUID,
						nodeUUID);
				navigateToRelativeTargetItem(targetItem, nodeRelpath,
						propertyRelPath);

				if ((LOG.isDebugEnabled()) && (targetItem.getItem() == null)) {
					LOG.debug(JcrMessages.noNodeFor(
							targetItem.getAbsolutePath()).getMessage());
				}

				return targetItem.getItem();
			}

		} catch (final RepositoryException re) {
			LOG.warn(JcrMessages.noNodeFor("UUID=" + nodeUUID).getMessage());
		}

		return null;
	}

	private static void navigateToRelativeTargetItem(
			final TargetItem targetItem, final String nodeRelpath,
			final String propertyRelPath) throws RepositoryException,
			PathNotFoundException {

		if (nodeRelpath != null) {
			final Node node = targetItem.asNodeOrNull();

			if (node != null) {
				targetItem.setAbsolutePath(targetItem.getAbsolutePath() + "/"
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
			final Node node = targetItem.asNodeOrNull();

			if (node != null) {
				targetItem.setAbsolutePath(targetItem.getAbsolutePath() + "/"
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

	public static JcrMessage newJcrMessage(final Event event,
			final Session session,
			final JcrContentPayloadType contentPayloadType)
			throws RepositoryException {

		final EventContent eventContent = JcrEventUtils.getEventContent(event,
				session, contentPayloadType);

		return new JcrMessage(event.getPath(), event.getType(), JcrEventUtils
				.getEventTypeNameFromValue(event.getType()), event.getUserID(),
				eventContent.getData(), eventContent.getUuid());
	}

}
