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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.jcr.Item;
import javax.jcr.Property;
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
 * All the necessary goodness for building a <code>JcrMessage</code>.
 * 
 * @author David Dossot (david@dossot.net)
 */
class JcrMessageFactory {

	private static final Log LOG = LogFactory.getLog(JcrMessageFactory.class);

	static JcrMessage newInstance(Event event, Session session,
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

	private static Serializable outputProperty(String propertyPath,
			Property property, JcrContentPayloadType contentPayloadType)
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
			} else if (propertyType == PropertyType.BOOLEAN) {
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
		} catch (Exception e) {
			// log error but do not break message building
			LOG.error("Can not fetch property value for: " + propertyPath, e);
		}

		return result;
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
