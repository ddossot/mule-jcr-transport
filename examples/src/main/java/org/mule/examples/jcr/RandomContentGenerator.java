/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.examples.jcr;

import java.io.ByteArrayInputStream;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.TimerTask;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Generate random content in a JCR repository.
 * 
 * @author David Dossot
 */
public class RandomContentGenerator extends TimerTask {
	private static final String TEST_NODE_NAME = "testNode";

	private static final Random RANDOM = new Random();

	private Repository repository;

	private String username;

	private String password;

	private String rootNodeName;

	public void initialize() throws LoginException, RepositoryException {
		Session session = newSession();

		Node repositoryRoot = session.getRootNode();

		if (!repositoryRoot.hasNode(rootNodeName)) {
			repositoryRoot.addNode(rootNodeName);
		} else {
			repositoryRoot.getNode(rootNodeName);
		}

		session.save();
		session.logout();
	}

	private Session newSession() throws LoginException, RepositoryException {
		return repository.login(new SimpleCredentials(username, password
				.toCharArray()));
	}

	public void run() {
		try {
			Session session = newSession();
			Node testContentRoot = session.getRootNode().getNode(
					rootNodeName);

			int randomAction = RANDOM.nextInt(3);

			if (randomAction == 0) {
				// Node action
				if (testContentRoot.hasNode(TEST_NODE_NAME)) {
					testContentRoot.getNode(TEST_NODE_NAME).remove();
				} else {
					addRandomPropery(testContentRoot.addNode(TEST_NODE_NAME));
				}
			} else if (randomAction == 1) {
				String propertyName = getPropertyName(testContentRoot);

				if (testContentRoot.hasProperty(propertyName)) {
					testContentRoot.getProperty(
							getPropertyName(testContentRoot)).remove();
				} else {
					addRandomPropery(testContentRoot);
				}
			} else {
				String propertyName = getPropertyName(testContentRoot);

				if (testContentRoot.hasProperty(propertyName)) {
					Property property = testContentRoot
							.getProperty(getPropertyName(testContentRoot));

					// modify to itself just to generate a modify event
					property.setValue(property.getValue());
				} else {
					addRandomPropery(testContentRoot);
				}
			}

			session.save();
			session.logout();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addRandomPropery(Node target) throws ValueFormatException,
			VersionException, LockException, ConstraintViolationException,
			RepositoryException {

		switch (RANDOM.nextInt(7)) {
		case 0:
			target.setProperty(getPropertyName(target), RANDOM.nextBoolean());
			break;

		case 1:
			target.setProperty(getPropertyName(target), RANDOM.nextInt());
			break;

		case 2:
			target.setProperty(getPropertyName(target), RANDOM.nextDouble());
			break;

		case 3:
			target.setProperty(getPropertyName(target), RANDOM.nextLong());
			break;

		case 4:
			target
					.setProperty(getPropertyName(target),
							new GregorianCalendar());
			break;

		case 5:
			target.setProperty(getPropertyName(target), RandomStringUtils
					.randomAlphanumeric(16));
			break;

		case 6:
			target.setProperty(getPropertyName(target),
					new ByteArrayInputStream(RandomStringUtils.random(256)
							.getBytes()));
			break;
		}
	}

	private static String getPropertyName(Node target)
			throws RepositoryException {
		return target.getName() + "Property";
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setRootNodeName(String rootNodeName) {
		this.rootNodeName = rootNodeName;
	}

}
