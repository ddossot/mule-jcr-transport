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

import java.util.List;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.UnhandledException;
import org.mule.providers.AbstractConnector;
import org.mule.providers.FatalConnectException;
import org.mule.providers.jcr.handlers.NodeTypeHandler;
import org.mule.providers.jcr.handlers.NodeTypeHandlerManager;
import org.mule.providers.jcr.i18n.JcrMessages;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.util.ClassUtils;

/**
 * <code>JcrConnector</code> is a transport that connects to JCR 1.0 (aka JSR
 * 170) repositories.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrConnector extends AbstractConnector {

	// TODO add transaction support
	// TODO add streaming support

	private Repository repository;

	private String workspaceName;

	private String username;

	private String password;

	private Integer eventTypes;

	private Boolean deep;

	private List uuid;

	private List nodeTypeName;

	private Boolean noLocal;

	private String contentPayloadType;

	private final NodeTypeHandlerManager nodeTypeHandlerManager;

	/**
	 * Property that defines a relative path to append at the end of the target
	 * item path.
	 */
	public static final String JCR_PROPERTY_REL_PATH_PROPERTY = "propertyRelPath";

	/**
	 * Property that defines a relative path to append after the endpoint item
	 * path.
	 */
	public static final String JCR_NODE_RELPATH_PROPERTY = "nodeRelPath";

	/**
	 * Property that forces the creation of a child node under the node target
	 * by the endpoint URI, instead of trying first to locate an existing one.
	 */
	public static final String JCR_ALWAYS_CREATE_CHILD_NODE_PROPERTY = "alwaysCreate";

	/**
	 * Property that defines a particular node type name.
	 */
	public static final String JCR_NODE_TYPE_NAME_PROPERTY = "nodeTypeName";

	/**
	 * Property that forces the lookup of a particular node by UUID.
	 */
	public static final String JCR_NODE_UUID_PROPERTY = "nodeUUID";

	public JcrConnector() {
		super();

		setDefaultEndpointValues();

		nodeTypeHandlerManager = new NodeTypeHandlerManager();
	}

	public void doInitialise() throws InitialisationException {
		// Future JCR version will offer a standard way to get a repository
		// instance, so injecting it in the connector will become optional at
		// that time
		if (getRepository() == null) {
			throw new InitialisationException(JcrMessages
					.missingDependency("repository"), this);
		}

	}

	public void doConnect() throws Exception {
		// NOOP
	}

	public void doStart() throws org.mule.umo.UMOException {
		// NOOP
	}

	public void doStop() throws org.mule.umo.UMOException {
		// NOOP
	}

	public void doDisconnect() throws Exception {
		// NOOP
	}

	public void doDispose() {
		// NOOP
	}

	public Session newSession() throws RepositoryException {
		if (logger.isDebugEnabled()) {
			logger.debug("Opening new JCR session.");
		}

		Credentials credentials = ((getUsername() != null) && (getPassword() != null)) ? new SimpleCredentials(
				getUsername(), getPassword().toCharArray())
				: null;

		return getRepository().login(credentials, getWorkspaceName());
	}

	public void terminateSession(Session session) {
		if ((session != null) && (session.isLive())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Terminating JCR session");
			}

			session.logout();
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Can not terminate session: " + session);
			}
		}
	}

	public Session validateSession(Session session) {
		if ((session != null) && (session.isLive())) {
			return session;
		} else {
			logger.info("JCR session is invalid: a new one will be created.");

			try {
				return newSession();
			} catch (RepositoryException re) {
				logger.error("Impossible to reconnect to the JCR container!",
						re);

				if (connectionStrategy != null) {
					initialised.set(false);

					try {
						stopConnector();
					} catch (UMOException umoe) {
						logger.warn(umoe.getMessage(), umoe);
					}

					try {
						connectionStrategy.connect(this);
						initialise();
						startConnector();
						return session;

					} catch (FatalConnectException fce) {
						throw new IllegalStateException(
								"Failed to reconnect to JCR server. I'm giving up.");
					} catch (UMOException umoe) {
						throw new UnhandledException(
								"Failed to recover a connector.", umoe);
					}

				} else {
					throw new IllegalStateException(
							"Connection to the JCR container has been lost "
									+ "and no connection strategy has been defined on the connector!");
				}
			}
		}
	}

	private void setDefaultEndpointValues() {
		// any change here must be reflected in the documentation
		setContentPayloadType(JcrContentPayloadType.NONE.toString());
		setEventTypes(new Integer(0));
		setDeep(Boolean.FALSE);
		setNoLocal(Boolean.TRUE);
		setUuid(null);
		setNodeTypeName(null);
	}

	public String getProtocol() {
		return "jcr";
	}

	/**
	 * Sets an optional list of <code>NodeTypeHandlers</code> class names.
	 * 
	 * @param customNodeTypeHandlers
	 */
	public void setCustomNodeTypeHandlers(List customNodeTypeHandlers) {
		// TODO create test case
		if (customNodeTypeHandlers != null) {
			for (int i = 0; i < customNodeTypeHandlers.size(); i++) {
				String customNodeTypeHandlerClassName = customNodeTypeHandlers
						.get(i).toString();

				try {
					NodeTypeHandler handler = (NodeTypeHandler) ClassUtils
							.instanciateClass(customNodeTypeHandlerClassName,
									ClassUtils.NO_ARGS, this.getClass());

					getNodeTypeHandlerManager().registerHandler(handler);
				} catch (Exception e) {
					logger.error("Can not load custom type handler: "
							+ customNodeTypeHandlerClassName, e);
				}
			}
		}

	}

	/**
	 * @return the nodeTypeHandlerManager
	 */
	public NodeTypeHandlerManager getNodeTypeHandlerManager() {
		return nodeTypeHandlerManager;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * @param repository
	 *            the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the workspaceName
	 */
	public String getWorkspaceName() {
		return workspaceName;
	}

	/**
	 * @param workspaceName
	 *            the workspaceName to set
	 */
	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	/**
	 * @return the deep
	 */
	public Boolean isDeep() {
		return deep;
	}

	/**
	 * @param deep
	 *            the deep to set
	 */
	public void setDeep(Boolean deep) {
		this.deep = deep;
	}

	/**
	 * @return the eventTypes
	 */
	public Integer getEventTypes() {
		return eventTypes;
	}

	/**
	 * @param eventTypes
	 *            the eventTypes to set
	 */
	public void setEventTypes(Integer eventTypes) {
		this.eventTypes = eventTypes;
	}

	/**
	 * @return the nodeTypeName
	 */
	public List getNodeTypeName() {
		return nodeTypeName;
	}

	/**
	 * @param nodeTypeName
	 *            the nodeTypeName to set
	 */
	public void setNodeTypeName(List nodeTypeName) {
		this.nodeTypeName = nodeTypeName;
	}

	/**
	 * @return the noLocal
	 */
	public Boolean isNoLocal() {
		return noLocal;
	}

	/**
	 * @param noLocal
	 *            the noLocal to set
	 */
	public void setNoLocal(Boolean noLocal) {
		this.noLocal = noLocal;
	}

	/**
	 * @return the uuid
	 */
	public List getUuid() {
		return uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(List uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the contentPayloadType
	 */
	public String getContentPayloadType() {
		return contentPayloadType;
	}

	/**
	 * @param contentPayloadType
	 *            the contentPayloadType to set
	 */
	public void setContentPayloadType(String contentPayloadType) {
		this.contentPayloadType = contentPayloadType;
	}

}
