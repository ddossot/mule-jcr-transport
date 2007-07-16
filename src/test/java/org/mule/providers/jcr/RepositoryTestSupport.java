package org.mule.providers.jcr;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.core.TransientRepository;

/**
 * The Singleton Strikes Back!
 * @author David Dossot
 */
public abstract class RepositoryTestSupport {

	public static final String USERNAME = "admin";

	public static final String PASSWORD = "admin";

	private static final String ROOT_NODE_NAME = "testData";

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

			Node root = session.getRootNode();

			if (root.hasNode(ROOT_NODE_NAME)) {
				root.getNode(ROOT_NODE_NAME).remove();
			}

			testDataNode = root.addNode(ROOT_NODE_NAME);
			session.save();
		}

		return repository;
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
