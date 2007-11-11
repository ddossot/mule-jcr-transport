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
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.UnhandledException;
import org.mule.providers.AbstractConnector;
import org.mule.providers.FatalConnectException;
import org.mule.providers.jcr.i18n.JcrMessages;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;

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

	private Session session;

	private Integer eventTypes;

	private Boolean deep;

	private List uuid;

	private List nodeTypeName;

	private Boolean noLocal;

	private String contentPayloadType;

	/**
	 * Event property to define a relative path to append at the end of the
	 * target item path.
	 */
	public static final String JCR_PROPERTY_REL_PATH_PROPERTY = "jcr.propertyRelPath";

	/**
	 * Event property to define a relative path to append after the endpoint
	 * item path.
	 */
	public static final String JCR_NODE_RELPATH_PROPERTY = "jcr.nodeRelpath";

	/**
	 * Event property to force the lookup of a particular node by UUID.
	 */
	public static final String JCR_NODE_UUID_PROPERTY = "jcr.nodeUUID";

	public JcrConnector() {
		super();

		setDefaultEndpointValues();
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
		Credentials credentials = ((getUsername() != null) && (getPassword() != null)) ? new SimpleCredentials(
				getUsername(), getPassword().toCharArray())
				: null;

		session = getRepository().login(credentials, getWorkspaceName());
	}

	public void doStart() throws org.mule.umo.UMOException {
		// NOOP
	}

	public void doStop() throws org.mule.umo.UMOException {
		// NOOP
	}

	public void doDisconnect() throws Exception {
		if (session != null) {
			session.logout();
		}
	}

	public void doDispose() {
		// NOOP
	}

	public Session getSession() {
		if ((session != null) && (session.isLive())) {
			return session;
		} else {
			if (connectionStrategy != null) {
				logger.info("JCR session is invalid and will be recycled.");

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
