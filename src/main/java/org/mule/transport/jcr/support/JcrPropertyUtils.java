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
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.api.routing.filter.Filter;
import org.mule.routing.filters.logic.AndFilter;
import org.mule.transport.jcr.JcrConnector;
import org.mule.transport.jcr.JcrContentPayloadType;
import org.mule.transport.jcr.filters.AbstractJcrNameFilter;
import org.mule.transport.jcr.i18n.JcrMessages;
import org.mule.util.IOUtils;

/**
 * @author David Dossot (david@dossot.net)
 */
public abstract class JcrPropertyUtils {
	private static final Log LOG = LogFactory.getLog(JcrPropertyUtils.class);

	public static Serializable getNonBinaryPropertyValue(
			final Value propertyValue, final int propertyType)
			throws ValueFormatException, RepositoryException {

		Serializable result;

		if (propertyType == PropertyType.BOOLEAN) {
			result = Boolean.valueOf(propertyValue.getBoolean());
		} else if (propertyType == PropertyType.DATE) {
			result = propertyValue.getDate();
		} else if (propertyType == PropertyType.DOUBLE) {
			result = Double.valueOf(propertyValue.getDouble());
		} else if (propertyType == PropertyType.LONG) {
			result = Long.valueOf(propertyValue.getLong());
		} else {
			result = propertyValue.getString();
		}

		return result;
	}

	public static Map<String, Object> getPropertiesPayload(
			final PropertyIterator propertyIterator)
			throws RepositoryException, ValueFormatException {

		final Map<String, Object> result = new HashMap<String, Object>();

		while (propertyIterator.hasNext()) {
			final Property property = (Property) propertyIterator.next();
			result.put(property.getName(), getPropertyPayload(property));
		}

		return result.isEmpty() ? null : result;
	}

	public static String getPropertyNamePatternFilter(final Filter filter,
			final Class<?> filterClass) {

		String pattern = null;

		if (filter != null) {
			if (filter instanceof AbstractJcrNameFilter) {
				if (filter.getClass().equals(filterClass)) {
					pattern = ((AbstractJcrNameFilter) filter).getPattern();
				}
			} else if (filter instanceof AndFilter) {
				final AndFilter andFilter = (AndFilter) filter;

				pattern = getPropertyNamePatternFilter(andFilter.getFilters()
						.get(0), filterClass);

				if (pattern == null) {
					pattern = getPropertyNamePatternFilter(andFilter
							.getFilters().get(1), filterClass);
				}
			} else {
				throw new IllegalArgumentException(JcrMessages.badFilterType(
						filter.getClass()).getMessage());
			}
		}

		return pattern;
	}

	public static String getPropertyRelPath(final MuleEvent event) {
		return event != null ? JcrEventUtils.getParsableEventProperty(event,
				JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY) : null;
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

	public static Value[] newPropertyValues(final Session session,
			final Collection<?> objects) throws RepositoryException,
			IOException {

		final Value[] values = new Value[objects.size()];

		int i = 0;

		for (final Object object : objects) {
			values[i++] = newPropertyValue(session, object);
		}

		return values;
	}

	public static Serializable outputPropertyValue(final String propertyPath,
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
				result = JcrPropertyUtils.getNonBinaryPropertyValue(
						propertyValue, propertyType);
			}
		} catch (final RuntimeException e) {
			JcrPropertyUtils.logPropertyAccessError(propertyPath, e);
		} catch (final ValueFormatException vfe) {
			JcrPropertyUtils.logPropertyAccessError(propertyPath, vfe);
		} catch (final RepositoryException re) {
			JcrPropertyUtils.logPropertyAccessError(propertyPath, re);
		} catch (final IOException ioe) {
			JcrPropertyUtils.logPropertyAccessError(propertyPath, ioe);
		}

		return result;
	}

	public static void storeProperties(final Session session,
			final Node targetNode, final Map<String, ?> propertyNamesAndValues)
			throws RepositoryException, IOException {

		for (final Map.Entry<String, ?> propertyNameAndValue : propertyNamesAndValues
				.entrySet()) {

			final String propertyName = propertyNameAndValue.getKey();
			final Object propertyValue = propertyNameAndValue.getValue();

			if ((propertyValue instanceof Collection<?>)) {
				targetNode.setProperty(propertyName, JcrPropertyUtils
						.newPropertyValues(session,
								(Collection<?>) propertyValue));
			} else {
				targetNode.setProperty(propertyName, JcrPropertyUtils
						.newPropertyValue(session, propertyValue));
			}
		}
	}

	private static void logPropertyAccessError(final String propertyPath,
			final Exception e) {
		LOG.error("Can not fetch property value for: " + propertyPath, e);
	}

	static Object getPropertyPayload(final Property property)
			throws IllegalStateException, ValueFormatException,
			RepositoryException {

		if (property.getDefinition().isMultiple()) {
			final List<Object> valuePayloads = new ArrayList<Object>();

			final Value[] propertyValues = property.getValues();

			for (int i = 0; i < propertyValues.length; i++) {
				valuePayloads.add(JcrPropertyUtils
						.getValuePayload(propertyValues[i]));
			}

			return valuePayloads;
		}

		return JcrPropertyUtils.getValuePayload(property.getValue());
	}

	static Object getValuePayload(final Value value)
			throws IllegalStateException, RepositoryException {

		final int propertyType = value.getType();

		if (propertyType == PropertyType.BINARY) {
			return value.getStream();
		}

		return getNonBinaryPropertyValue(value, propertyType);
	}

	static Serializable outputProperty(final String propertyPath,
			final Property property,
			final JcrContentPayloadType contentPayloadType)
			throws RepositoryException, ValueFormatException {

		Serializable result;

		if (property.getDefinition().isMultiple()) {
			final ArrayList<Serializable> contentList = new ArrayList<Serializable>();

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

	private JcrPropertyUtils() {
		throw new UnsupportedOperationException("Do not instantiate");
	}

}
