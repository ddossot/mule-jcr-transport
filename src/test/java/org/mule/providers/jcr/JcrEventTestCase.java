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
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.observation.Event;

import junit.framework.TestCase;

import com.thoughtworks.xstream.XStream;

/**
 * @author David Dossot
 */
public class JcrEventTestCase extends TestCase {
	private static final XStream XSTREAM = new XStream();

	private static final String USER_ID = "jqdoe";

	private static class DummyEvent implements Event {
		private final String path;

		private final int type;

		private final String userID;

		DummyEvent(final String path, final int type, final String userID) {
			this.path = path;
			this.type = type;
			this.userID = userID;
		}

		public String getPath() {
			return path;
		}

		public int getType() {
			return type;
		}

		public String getUserID() {
			return userID;
		}
	}

	public void testGetEventTypeNameFromValueMarginalCases() {
		assertEquals(JcrMessage.UNKNOWN_EVENT_TYPE, JcrMessageUtils
				.getEventTypeNameFromValue(Integer.MIN_VALUE));
	}

	public void testIgnoredEventTypes() throws Exception {
		Property property = RepositoryTestSupport.getTestDataNode()
				.setProperty("ignored", "fooValue");
		RepositoryTestSupport.getSession().save();

		testContentEventType(property.getPath(), Event.NODE_ADDED, "");
		testContentEventType(property.getPath(), Event.NODE_REMOVED, "");
		testContentEventType(property.getPath(), Event.PROPERTY_REMOVED, "");
	}

	public void testKeptEventTypes() throws Exception {
		Property property = RepositoryTestSupport.getTestDataNode()
				.setProperty("kept", "content");
		RepositoryTestSupport.getSession().save();

		testContentEventType(property.getPath(), Event.PROPERTY_ADDED, property
				.getString());

		testContentEventType(property.getPath(), Event.PROPERTY_CHANGED,
				property.getString());
	}

	public void testUnreachableProperty() throws Exception {
		testContentEventType("/foo/bar", Event.PROPERTY_ADDED, "");
	}

	public void testBinaryProperty() throws Exception {
		byte[] binaryContent = "binary.content".getBytes();

		Property property = RepositoryTestSupport.getTestDataNode()
				.setProperty("binary", new ByteArrayInputStream(binaryContent));

		RepositoryTestSupport.getSession().save();

		testContentEventType(JcrContentPayloadType.FULL, property.getPath(),
				Event.PROPERTY_ADDED, binaryContent);

		testContentEventType(JcrContentPayloadType.NO_BINARY, property
				.getPath(), Event.PROPERTY_ADDED, "");
	}

	public void testBooleanProperty() throws Exception {
		Property property = RepositoryTestSupport.getTestDataNode()
				.setProperty("boolean", true);

		RepositoryTestSupport.getSession().save();

		testContentEventType(JcrContentPayloadType.NO_BINARY, property
				.getPath(), Event.PROPERTY_ADDED, Boolean.TRUE);
	}

	public void testDoubleProperty() throws Exception {
		Double doubleContent = new Double(3.14d);

		Property property = RepositoryTestSupport.getTestDataNode()
				.setProperty("double", doubleContent.doubleValue());

		RepositoryTestSupport.getSession().save();

		testContentEventType(JcrContentPayloadType.NO_BINARY, property
				.getPath(), Event.PROPERTY_ADDED, doubleContent);
	}

	public void testLongProperty() throws Exception {
		Long longContent = new Long(149);

		Property property = RepositoryTestSupport.getTestDataNode()
				.setProperty("long", longContent.longValue());

		RepositoryTestSupport.getSession().save();

		testContentEventType(JcrContentPayloadType.NO_BINARY, property
				.getPath(), Event.PROPERTY_ADDED, longContent);
	}

	public void testCalendarProperty() throws Exception {
		GregorianCalendar calendar = new GregorianCalendar(1969, 7, 21, 2, 56,
				0);

		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));

		Property property = RepositoryTestSupport.getTestDataNode()
				.setProperty("calendar", calendar);

		RepositoryTestSupport.getSession().save();

		testContentEventType(JcrContentPayloadType.NO_BINARY, property
				.getPath(), Event.PROPERTY_ADDED, calendar);
	}

	public void testNodeProperty() throws Exception {
		Node testDataNode = RepositoryTestSupport.getTestDataNode();

		Node targetNode = testDataNode.addNode("target");
		targetNode.addMixin("mix:referenceable");

		Property property = testDataNode.setProperty("node", targetNode);

		RepositoryTestSupport.getSession().save();

		testContentEventType(JcrContentPayloadType.NO_BINARY, property
				.getPath(), Event.PROPERTY_ADDED, targetNode.getUUID());
	}

	public void testMultipleProperty() throws Exception {
		String[] values = new String[] { "one", "two" };

		Property property = RepositoryTestSupport.getTestDataNode()
				.setProperty("multi", values);

		RepositoryTestSupport.getSession().save();

		testContentEventType(property.getPath(), Event.PROPERTY_CHANGED, Arrays
				.asList(values));
	}

	public void testExceptionWhenGettingValue() {
		assertEquals("", JcrMessageUtils.outputPropertyValue("/foo/bar",
				null, null));
	}

	private void testContentEventType(String propertyPath, int eventType,
			Object expectedContent) throws Exception {

		testContentEventType(JcrContentPayloadType.FULL, propertyPath,
				eventType, expectedContent);
	}

	private void testContentEventType(
			JcrContentPayloadType jcrContentPayloadType, String propertyPath,
			int eventType, Object expectedContent) throws Exception {

		JcrMessage jcrEvent = JcrMessageUtils.newInstance(new DummyEvent(
				propertyPath, eventType, USER_ID), RepositoryTestSupport
				.getSession(), jcrContentPayloadType);

		testXStreamSerialization(jcrEvent);

		assertNotNull(jcrEvent.toString());
		assertEquals(propertyPath, jcrEvent.getPath());
		assertEquals(USER_ID, jcrEvent.getUserID());

		assertEquals(JcrMessageUtils.getEventTypeNameFromValue(eventType),
				jcrEvent.getTypeAsString());

		if (expectedContent instanceof Collection) {
			assertTrue(jcrEvent.getContent() instanceof Collection);

			Collection colContent = (Collection) jcrEvent.getContent();
			Collection colExpectedContent = (Collection) expectedContent;

			assertTrue(colContent.containsAll(colExpectedContent));
			assertTrue(colExpectedContent.containsAll(colContent));
		} else if (expectedContent instanceof byte[]) {
			assertTrue(Arrays.equals((byte[]) expectedContent,
					(byte[]) jcrEvent.getContent()));
		} else {
			assertEquals(expectedContent, jcrEvent.getContent());
		}
	}

	private void testXStreamSerialization(JcrMessage jcrMessage) {
		assertEquals(jcrMessage, XSTREAM.fromXML(XSTREAM.toXML(jcrMessage)));
	}
}
