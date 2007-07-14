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

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.observation.Event;

import junit.framework.TestCase;

import org.apache.jackrabbit.core.TransientRepository;

/**
 * @author David Dossot
 */
public class JcrEventTestCase extends TestCase {
	private Repository repository;

	private Session session;

	private Node testDataNode;

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

	public JcrEventTestCase() throws Exception {
		repository = new TransientRepository();

		session = repository.login(new SimpleCredentials("admin", "admin"
				.toCharArray()));

		Node root = session.getRootNode();

		if (root.hasNode("testData")) {
			root.getNode("testData").remove();
		}

		testDataNode = root.addNode("testData");
		session.save();
	}

	public void testIgnoredEventTypes() throws Exception {
		// TODO test all ignored types
		Property property = testDataNode.setProperty("ignored", "fooValue");
		session.save();

		SerializableJcrEvent jcrEvent = JcrEvent.newInstance(new DummyEvent(
				property.getPath(), Event.PROPERTY_REMOVED, "fooUserID"),
				session, JcrContentPayloadType.FULL);

		assertEquals(property.getPath(), jcrEvent.getPath());
		assertEquals("fooUserID", jcrEvent.getUserID());
		assertEquals("PROPERTY_REMOVED", jcrEvent.getType());
		assertEquals("", jcrEvent.getContent());
	}
	
	//TODO test non ignored event types
	//TODO test all property types, including multiple

}
