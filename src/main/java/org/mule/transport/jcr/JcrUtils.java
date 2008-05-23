/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.observation.Event;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.routing.filter.Filter;
import org.mule.routing.filters.logic.AndFilter;
import org.mule.transport.jcr.filters.AbstractJcrNameFilter;
import org.mule.transport.jcr.i18n.JcrMessages;
import org.mule.util.DateUtils;
import org.mule.util.IOUtils;
import org.mule.util.TemplateParser;
import org.mule.util.UUID;

/**
 * Utility class that provides methods for "detaching" JCR events and content
 * from the container so they can be used as payload that survives the closing
 * of the session.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrUtils {

	public static final String DEFAULT_DATE_FORMAT = "dd-MM-yy_HH-mm-ss.SSS";

	private static final TemplateParser ANT_PARSER = TemplateParser
			.createAntStyleParser();

	private static final Log LOG = LogFactory.getLog(JcrUtils.class);

	public static JcrMessage newJcrMessage(final Event event,
			final Session session,
			final JcrContentPayloadType contentPayloadType)
			throws RepositoryException {

		final EventContent eventContent = getEventContent(event, session,
				contentPayloadType);

		return new JcrMessage(event.getPath(), event.getType(),
				getEventTypeNameFromValue(event.getType()), event.getUserID(),
				eventContent.getData(), eventContent.getUuid());
	}

	static class EventContent {
		private Serializable data;

		private String uuid;

		public EventContent() {
			setData("");
			setUuid(null);
		}

		public Serializable getData() {
			return data;
		}

		public void setData(final Serializable data) {
			this.data = data;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(final String uuid) {
			this.uuid = uuid;
		}
	}

	static EventContent getEventContent(final Event event,
			final Session session,
			final JcrContentPayloadType contentPayloadType) {

		final EventContent result = new EventContent();

		if (!JcrContentPayloadType.NONE.equals(contentPayloadType)) {

			final int eventType = event.getType();

			// tentatively add content from the path of the event if the
			// event is not a removal if the content can not be fetched (because
			// it has changed between the moment the event was raised and the
			// moment we build this message), report the error at info level
			// only (this is a failure that can happen and is not business
			// critical in any way).
			String eventPath = "N/A";

			try {
				if ((eventType == Event.PROPERTY_ADDED)
						|| (eventType == Event.PROPERTY_CHANGED)) {

					eventPath = event.getPath();
					final Item item = session.getItem(eventPath);

					if (!item.isNode()) {
						// is not a node == is a property
						result.setData(outputProperty(eventPath,
								(Property) item, contentPayloadType));
					}

				} else if (eventType == Event.NODE_ADDED) {
					eventPath = event.getPath();
					final Item item = session.getItem(eventPath);

					if (item.isNode()) {
						final Node node = ((Node) item);
						if (node.isNodeType("mix:referenceable")) {
							result.setUuid(node.getUUID());
						}
					}
				}
			} catch (final RepositoryException ignoredException) {
				if (LOG.isInfoEnabled()) {
					LOG.info("Can not fetch content for event path: "
							+ eventPath + "(" + ignoredException.getMessage()
							+ ")");
				}
			}

		}

		return result;
	}

	static Serializable outputProperty(final String propertyPath,
			final Property property,
			final JcrContentPayloadType contentPayloadType)
			throws RepositoryException, ValueFormatException {

		Serializable result;

		if (property.getDefinition().isMultiple()) {
			final ArrayList contentList = new ArrayList();

			final Value[] propertyValues = property.getValues();

			for (int i = 0; i < propertyValues.length; i++) {
				contentList.add(outputPropertyValue(propertyPath,
						propertyValues[i], contentPayloadType));
			}

			result = contentList;
		} else {
			result = outputPropertyValue(propertyPath, property.getValue(),
					contentPayloadType);
		}

		return result;
	}

	static Serializable outputPropertyValue(final String propertyPath,
			final Value propertyValue,
			final JcrContentPayloadType contentPayloadType) {

		Serializable result = "";

		try {
			final int propertyType = propertyValue.getType();

			if (propertyType == PropertyType.BINARY) {
				if (!JcrContentPayloadType.NO_BINARY.equals(contentPayloadType)) {
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					IOUtils.copy(propertyValue.getStream(), baos);
					result = baos.toByteArray();
				}
			} else {
				result = getNonBinaryPropertyValue(propertyValue, propertyType);
			}
		} catch (final RuntimeException e) {
			logPropertyAccessError(propertyPath, e);
		} catch (final ValueFormatException vfe) {
			logPropertyAccessError(propertyPath, vfe);
		} catch (final RepositoryException re) {
			logPropertyAccessError(propertyPath, re);
		} catch (final IOException ioe) {
			logPropertyAccessError(propertyPath, ioe);
		}

		return result;
	}

	private static void logPropertyAccessError(final String propertyPath,
			final Exception e) {
		LOG.error("Can not fetch property value for: " + propertyPath, e);
	}

	static Serializable getNonBinaryPropertyValue(final Value propertyValue,
			final int propertyType) throws ValueFormatException,
			RepositoryException {

		Serializable result;

		if (propertyType == PropertyType.BOOLEAN) {
			result = Boolean.valueOf(propertyValue.getBoolean());
		} else if (propertyType == PropertyType.DATE) {
			result = propertyValue.getDate();
		} else if (propertyType == PropertyType.DOUBLE) {
			result = new Double(propertyValue.getDouble());
		} else if (propertyType == PropertyType.LONG) {
			result = new Long(propertyValue.getLong());
		} else {
			result = propertyValue.getString();
		}

		return result;
	}

	public static Object getValuePayload(final Value value)
			throws IllegalStateException, RepositoryException {

		final int propertyType = value.getType();

		if (propertyType == PropertyType.BINARY) {
			return value.getStream();
		} else {
			return getNonBinaryPropertyValue(value, propertyType);
		}
	}

	static Object getPropertyPayload(final Property property)
			throws IllegalStateException, ValueFormatException,
			RepositoryException {

		if (property.getDefinition().isMultiple()) {
			final List valuePayloads = new ArrayList();

			final Value[] propertyValues = property.getValues();

			for (int i = 0; i < propertyValues.length; i++) {
				valuePayloads.add(getValuePayload(propertyValues[i]));
			}

			return valuePayloads;
		} else {
			return getValuePayload(property.getValue());
		}
	}

	public static void storeProperties(final Session session,
			final Node targetNode, final Map propertyNamesAndValues)
			throws RepositoryException, IOException {

		for (final Iterator i = propertyNamesAndValues.entrySet().iterator(); i
				.hasNext();) {

			final Map.Entry propertyNameAndValue = (Entry) i.next();

			final String propertyName = (String) propertyNameAndValue.getKey();
			final Object propertyValue = propertyNameAndValue.getValue();

			if ((propertyValue instanceof Collection)) {
				targetNode
						.setProperty(propertyName, JcrUtils.newPropertyValues(
								session, (Collection) propertyValue));
			} else {
				targetNode.setProperty(propertyName, JcrUtils.newPropertyValue(
						session, propertyValue));
			}
		}
	}

	public static Value[] newPropertyValues(final Session session,
			final Collection objects) throws RepositoryException, IOException {

		final Value[] values = new Value[objects.size()];

		int i = 0;

		for (final Iterator j = objects.iterator(); j.hasNext();) {
			values[i++] = newPropertyValue(session, j.next());
		}

		return values;
	}

	public static Value newPropertyValue(final Session session,
			final Object value) throws RepositoryException, IOException {

		if (value == null) {
			throw new IllegalArgumentException(
					"Impossible to store a null value in JCR!");

		} else if (value instanceof Boolean) {
			return session.getValueFactory().createValue(
					((Boolean) value).booleanValue());

		} else if (value instanceof Calendar) {
			return session.getValueFactory().createValue((Calendar) value);

		} else if (value instanceof Double) {
			return session.getValueFactory().createValue(
					((Double) value).doubleValue());

		} else if (value instanceof InputStream) {
			return session.getValueFactory().createValue((InputStream) value);

		} else if (value instanceof byte[]) {
			return session.getValueFactory().createValue(
					new ByteArrayInputStream((byte[]) value));

		} else if (value instanceof Long) {
			return session.getValueFactory().createValue(
					((Long) value).longValue());

		} else if (value instanceof Node) {
			return session.getValueFactory().createValue((Node) value);

		} else if (value instanceof String) {
			return session.getValueFactory().createValue((String) value);

		} else if (value instanceof Serializable) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			oos.flush();
			oos.close();

			return session.getValueFactory().createValue(
					new ByteArrayInputStream(baos.toByteArray()));
		} else {
			throw new IllegalArgumentException(
					"Impossible to store object of type: " + value.getClass());
		}

	}

	public static Object getItemPayload(final Item item)
			throws IllegalStateException, ValueFormatException,
			RepositoryException {

		if (item.isNode()) {
			return getPropertiesPayload(((Node) item).getProperties());
		} else {
			return getPropertyPayload((Property) item);
		}
	}

	public static Map getPropertiesPayload(
			final PropertyIterator propertyIterator)
			throws RepositoryException, ValueFormatException {

		final Map result = new HashMap();

		while (propertyIterator.hasNext()) {
			final Property property = (Property) propertyIterator.next();
			result.put(property.getName(), getPropertyPayload(property));
		}

		return result.isEmpty() ? null : result;
	}

	// This should really be in JCR API!
	static String getEventTypeNameFromValue(final int eventType) {
		switch (eventType) {

		case Event.NODE_ADDED:
			return "NODE_ADDED";

		case Event.NODE_REMOVED:
			return "NODE_REMOVED";

		case Event.PROPERTY_ADDED:
			return "PROPERTY_ADDED";

		case Event.PROPERTY_CHANGED:
			return "PROPERTY_CHANGED";

		case Event.PROPERTY_REMOVED:
			return "PROPERTY_REMOVED";

		default:
			return JcrMessage.UNKNOWN_EVENT_TYPE;
		}
	}

	static String getParsableEventProperty(final MuleEvent event,
			final String propertyName) {
		return JcrUtils.parsePath((String) event
				.getProperty(propertyName, true), event);
	}

	static String parsePath(final String path, final MuleEvent event) {
		if ((path == null) || (path.indexOf('{') == -1)) {
			return path;
		}

		return ANT_PARSER.parse(new TemplateParser.TemplateCallback() {
			public Object match(String token) {

				if (token.equals("DATE")) {
					return DateUtils.getTimeStamp(DEFAULT_DATE_FORMAT);

				} else if (token.startsWith("DATE:")) {
					token = token.substring(5);
					return DateUtils.getTimeStamp(token);

				} else if (token.startsWith("UUID")) {
					return UUID.getUUID();

				} else if (token.startsWith("SYSTIME")) {
					return String.valueOf(System.currentTimeMillis());

				} else if (event != null) {
					return event.getProperty(token, true);
				}

				return null;
			}
		}, path);
	}

	static String getPropertyNamePatternFilter(final Filter filter,
			final Class filterClass) {

		String pattern = null;

		if (filter != null) {
			if (filter instanceof AbstractJcrNameFilter) {
				if (filter.getClass().equals(filterClass)) {
					pattern = ((AbstractJcrNameFilter) filter).getPattern();
				}
			} else if (filter instanceof AndFilter) {
				final AndFilter andFilter = (AndFilter) filter;

				pattern = getPropertyNamePatternFilter((Filter) andFilter
						.getFilters().get(0), filterClass);

				if (pattern == null) {
					pattern = getPropertyNamePatternFilter((Filter) andFilter
							.getFilters().get(1), filterClass);
				}
			} else {
				throw new IllegalArgumentException(JcrMessages.badFilterType(
						filter.getClass()).getMessage());
			}
		}

		return pattern;
	}

	private static String getNodeUUID(final MuleEvent event) {
		return event != null ? (String) JcrUtils.getParsableEventProperty(
				event, JcrConnector.JCR_NODE_UUID_PROPERTY) : null;
	}

	private static QueryDefinition getQueryDefinition(final MuleEvent event) {
		return event != null ? new QueryDefinition((String) event.getProperty(
				JcrConnector.JCR_QUERY_LANGUAGE_PROPERTY, true), JcrUtils
				.getParsableEventProperty(event,
						JcrConnector.JCR_QUERY_STATEMENT_PROPERTY)) : null;
	}

	private static String getPropertyRelPath(final MuleEvent event) {
		return event != null ? JcrUtils.getParsableEventProperty(event,
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY) : null;
	}

	private static String getNodeRelPath(final MuleEvent event) {
		return event != null ? JcrUtils.getParsableEventProperty(event,
				JcrConnector.JCR_NODE_RELPATH_PROPERTY) : null;
	}

	static Object getRawContentFromProperty(final Item targetItem)
			throws RepositoryException {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Getting payload for property: " + targetItem.getPath());
		}

		return targetItem;
	}

	static Object getRawContentFromNode(Item targetItem,
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
					result = null;
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

	static Item getTargetItem(final Session session,
			final ImmutableEndpoint endpoint, final MuleEvent event,
			final boolean navigateRelativePaths) throws RepositoryException,
			PathNotFoundException {

		final String nodeUUID = getNodeUUID(event);

		final QueryDefinition queryDefinition = getQueryDefinition(event);

		final String nodeRelPath = navigateRelativePaths ? getNodeRelPath(event)
				: null;

		final String propertyRelPath = navigateRelativePaths ? getPropertyRelPath(event)
				: null;

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

	private static Item getTargetItemFromNodeIterator(final String pathContext,
			final NodeIterator nodes) throws RepositoryException {

		if (nodes.getSize() == 0) {
			LOG.warn(JcrMessages.noNodeFor(pathContext).getMessage());

			return null;

		} else {
			if (nodes.getSize() > 1) {
				LOG.warn(JcrMessages.moreThanOneNodeFor(pathContext)
						.getMessage());
			}

			return nodes.nextNode();
		}
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

	private final static class QueryDefinition {
		private final String language;

		private final String statement;

		public QueryDefinition(final String language, final String statement) {
			this.language = language;
			this.statement = statement;
		}

		public String getLanguage() {
			return language;
		}

		public String getStatement() {
			return statement;
		}

		@Override
		public String toString() {
			return getLanguage() + ": " + getStatement();
		}

	}

	private static class TargetItem {
		private Item item;

		private String absolutePath;

		public TargetItem(final Item item, final String absolutePath) {
			this.item = item;
			this.absolutePath = absolutePath;
		}

		public Node asNodeOrNull() {
			return (item != null && item.isNode()) ? (Node) item : null;
		}

		public Item getItem() {
			return item;
		}

		public void setItem(final Item item) {
			this.item = item;
		}

		public String getAbsolutePath() {
			return absolutePath;
		}

		public void setAbsolutePath(final String absolutePath) {
			this.absolutePath = absolutePath;
		}
	}

}
