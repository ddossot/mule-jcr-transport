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

import java.util.GregorianCalendar;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

/**
 * Generate random content in a JCR repository.
 * 
 * @author David Dossot
 */
public class RandomContentGenerator {
	private Repository repository;

	private String username;

	private String password;

	private String rootNodeName;

	public void initialize() throws LoginException, RepositoryException {
		Session session = newSession();

		Node repositoryRoot = session.getRootNode();

		if (repositoryRoot.hasNode(rootNodeName)) {
			repositoryRoot.getNode(rootNodeName).remove();
		}

		Node rootNode = repositoryRoot.addNode(rootNodeName);

		// setup an empty target property for direct data writing
		rootNode.setProperty("targetProperty", "");

		// add some demo files
		Node imagesNode = rootNode.addNode("images");
		storeImage(imagesNode, "mule.gif");
		storeImage(imagesNode, "jackrabbit.gif");

		session.save();
		session.logout();
	}

	private void storeImage(Node imagesNode, String imageName)
			throws RepositoryException {

		Node imageNode = imagesNode.addNode(imageName, "nt:file");
		Node contentNode = imageNode.addNode("jcr:content", "nt:resource");
		contentNode.setProperty("jcr:data", Thread.currentThread()
				.getContextClassLoader().getResourceAsStream(
						"images/" + imageName));
		contentNode.setProperty("jcr:mimeType", "image/gif");
		contentNode.setProperty("jcr:lastModified", GregorianCalendar
				.getInstance());
	}

	private Session newSession() throws LoginException, RepositoryException {
		return repository.login(new SimpleCredentials(username, password
				.toCharArray()));
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
