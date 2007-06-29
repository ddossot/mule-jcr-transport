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
import org.mule.providers.jcr.i18n.JcrMessage;
import org.mule.umo.lifecycle.InitialisationException;

/**
 * <code>JcrConnector</code> TODO document
 */
public final class JcrConnector extends AbstractConnector {

    /*
     * For general guidelines on writing transports see
     * http://mule.mulesource.org/display/MULE/Writing+Transports
     */

    /*
     * IMPLEMENTATION NOTE: All configuaration for the transport should be set
     * on the Connector object, this is the object that gets configured in
     * MuleXml
     */

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

    private String contentPayload;

    public JcrConnector() {
        super();

        setDefaultEndpointValues();
    }

    public void doDispose() {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Should clean up any open resources associated
         * with the connector.
         */
    }

    public void doStop() throws org.mule.umo.UMOException {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Should put any associated resources into a
         * stopped state. Mule will automatically call the stop() method.
         */
    }

    public void doStart() throws org.mule.umo.UMOException {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: If there is a single server instance or
         * connection associated with the connector i.e. AxisServer or a Jms
         * Connection or Jdbc Connection, this method should put the resource in
         * a started state here.
         */
    }

    public void doDisconnect() throws Exception {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Disconnects any connections made in the connect
         * method If the connect method did not do anything then this method
         * shouldn't do anything either.
         */

        if (session != null) {
            session.logout();
        }
    }

    public void doConnect() throws Exception {
        // Optional; does not need to be implemented. Delete if not required

        /*
         * IMPLEMENTATION NOTE: Makes a connection to the underlying resource.
         * When connections are managed at the receiver/dispatcher level, this
         * method may do nothing
         */

        Credentials credentials =
                ((getUsername() != null) && (getPassword() != null)) ? new SimpleCredentials(
                        getUsername(), getPassword().toCharArray())
                        : null;

        session = getRepository().login(credentials, getWorkspaceName());
    }

    public void doInitialise() throws InitialisationException {
        // Future JCR version will offer a standard way to get a repository
        // instance, so injecting it in the connector will become optional at
        // that time
        if (getRepository() == null) {
            throw new InitialisationException(
                    JcrMessage.missingDependency("repository"), this);
        }

    }

    private void setDefaultEndpointValues() {
        setEventTypes(Integer.valueOf(0));
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
     * @return the contentPayload
     */
    public String getContentPayload() {
        return contentPayload;
    }

    /**
     * @param contentPayload
     *            the contentPayload to set
     */
    public void setContentPayload(String contentPayload) {
        this.contentPayload = contentPayload;
    }

}
