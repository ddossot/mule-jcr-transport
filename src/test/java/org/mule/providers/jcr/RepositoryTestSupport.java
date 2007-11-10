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

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.core.TransientRepository;

/**
 * The Singleton Strikes Back!
 * 
 * @author David Dossot
 */
public abstract class RepositoryTestSupport {

	public static final String USERNAME = "admin";

	public static final String PASSWORD = "admin";

	public static final String ROOT_NODE_NAME = "testData";

	private static Repository repository;

	private static Session session;

	private static Node testDataNode;

	private RepositoryTestSupport() {
		// NOOP
	}

	public synchronized static Repository getRepository() throws Exception {
		if (repository == null) {
			repository = new TransientRepository();

			session = repository.login(new SimpleCredentials("admin", "admin"
					.toCharArray()));

			resetRepository();
			session.save();
		}

		return repository;
	}

	public static void resetRepository() throws RepositoryException,
			VersionException, LockException, ConstraintViolationException,
			PathNotFoundException, ItemExistsException {

		Node root = session.getRootNode();

		if (root.hasNode(ROOT_NODE_NAME)) {
			root.getNode(ROOT_NODE_NAME).remove();
		}

		testDataNode = root.addNode(ROOT_NODE_NAME);
	}

	public synchronized static Session getSession() throws Exception {
		getRepository();

		return session;
	}

	public synchronized static Node getTestDataNode() throws Exception {
		getRepository();

		return testDataNode;
	}

}
