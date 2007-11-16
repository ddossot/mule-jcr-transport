/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.observation.Event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.util.IOUtils;

/**
 * Utility class that provides methods for "detaching" JCR events and content
 * from the container so they can be used as payload that survives the closing
 * of the session.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageUtils {

	private static final Log LOG = LogFactory.getLog(JcrMessageUtils.class);

	public static JcrMessage newInstance(Event event, Session session,
			JcrContentPayloadType contentPayloadType)
			throws RepositoryException {

		return new JcrMessage(event.getPath(), event.getType(),
				getEventTypeNameFromValue(event.getType()), event.getUserID(),
				getEventContent(event, session, contentPayloadType));
	}

	static Serializable getEventContent(Event event, Session session,
			JcrContentPayloadType contentPayloadType) {

		Serializable result = "";

		if (!JcrContentPayloadType.NONE.equals(contentPayloadType)) {

			int eventType = event.getType();

			// tentatively add content from the path of the event if the
			// event is not a removal if the content can not be fetched (because
			// it has changed between the moment the event was raised and the
			// moment we build this message), report the error at info level
			// only (this is a failure that can happen and is not business
			// critical in any way).
			if ((eventType == Event.PROPERTY_ADDED)
					|| (eventType == Event.PROPERTY_CHANGED)) {

				String eventPath = "N/A";

				try {
					eventPath = event.getPath();
					Item item = session.getItem(eventPath);

					if (!item.isNode()) {
						// is not a node == is a property
						result = outputProperty(eventPath, (Property) item,
								contentPayloadType);
					}

				} catch (RepositoryException ignoredException) {
					if (LOG.isInfoEnabled()) {
						LOG.info("Can not fetch content for event path: "
								+ eventPath + "("
								+ ignoredException.getMessage() + ")");
					}
				}
			}
		}

		return result;
	}

	static Serializable outputProperty(String propertyPath, Property property,
			JcrContentPayloadType contentPayloadType)
			throws RepositoryException, ValueFormatException {

		Serializable result;

		if (property.getDefinition().isMultiple()) {
			ArrayList contentList = new ArrayList();

			Value[] propertyValues = property.getValues();

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

	static Serializable outputPropertyValue(String propertyPath,
			Value propertyValue, JcrContentPayloadType contentPayloadType) {

		Serializable result = "";

		try {
			int propertyType = propertyValue.getType();

			if (propertyType == PropertyType.BINARY) {
				if (!JcrContentPayloadType.NO_BINARY.equals(contentPayloadType)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					IOUtils.copy(propertyValue.getStream(), baos);
					result = baos.toByteArray();
				}
			} else {
				result = getNonBinaryPropertyValue(propertyValue, propertyType);
			}
		} catch (Exception e) {
			// log error but do not break message building
			LOG.error("Can not fetch property value for: " + propertyPath, e);
		}

		return result;
	}

	static Serializable getNonBinaryPropertyValue(Value propertyValue,
			int propertyType) throws ValueFormatException, RepositoryException {

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

	static Object getValuePayload(Value value) throws IllegalStateException,
			RepositoryException {

		int propertyType = value.getType();

		if (propertyType == PropertyType.BINARY) {
			return value.getStream();
		} else {
			return getNonBinaryPropertyValue(value, propertyType);
		}
	}

	static Object getPropertyPayload(Property property)
			throws IllegalStateException, ValueFormatException,
			RepositoryException {

		if (property.getDefinition().isMultiple()) {
			List valuePayloads = new ArrayList();

			Value[] propertyValues = property.getValues();

			for (int i = 0; i < propertyValues.length; i++) {
				valuePayloads.add(getValuePayload(propertyValues[i]));
			}

			return valuePayloads;
		} else {
			return getValuePayload(property.getValue());
		}
	}

	static Value newPropertyValue(Session session, Object value)
			throws RepositoryException, IOException {

		// TODO unit test

		if (value == null) {
			// TODO check if this is valid in JCR
			return null;

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
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
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

	public static Object getItemPayload(Item item)
			throws IllegalStateException, ValueFormatException,
			RepositoryException {

		if (item.isNode()) {
			return getPropertiesPayload(((Node) item).getProperties());
		} else {
			return getPropertyPayload((Property) item);
		}
	}

	public static Map getPropertiesPayload(PropertyIterator propertyIterator)
			throws RepositoryException, ValueFormatException {

		Map result = new HashMap();

		while (propertyIterator.hasNext()) {
			Property property = (Property) propertyIterator.next();
			result.put(property.getName(), getPropertyPayload(property));
		}

		return result.isEmpty() ? null : result;
	}

	// This should really be in JCR API!
	static String getEventTypeNameFromValue(int eventType) {
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

}
