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

import org.mule.providers.AbstractConnector;
import org.mule.providers.jcr.i18n.JcrMessages;
import org.mule.umo.lifecycle.InitialisationException;

/**
 * <code>JcrConnector</code> is a transport that connects to JCR 1.0 (aka JSR
 * 170) repositories.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrConnector extends AbstractConnector {

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

	private void setDefaultEndpointValues() {
		// any change here must be reflected in the documentation
		setContentPayloadType(JcrContentPayloadType.NONE.toString());
		setEventTypes(new Integer(0));
		setDeep(Boolean.FALSE);
		setNoLocal(Boolean.TRUE);
		setUuid(null);
		setNodeTypeName(null);
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
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
