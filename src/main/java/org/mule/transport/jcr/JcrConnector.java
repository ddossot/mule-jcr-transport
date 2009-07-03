/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.Validate;
import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.retry.RetryCallback;
import org.mule.api.retry.RetryContext;
import org.mule.api.transport.ConnectorException;
import org.mule.api.transport.DispatchException;
import org.mule.config.i18n.CoreMessages;
import org.mule.model.streaming.CallbackOutputStream;
import org.mule.transport.AbstractConnector;
import org.mule.transport.jcr.config.JcrNamespaceHandler;
import org.mule.transport.jcr.handlers.NodeTypeHandler;
import org.mule.transport.jcr.handlers.NodeTypeHandlerManager;
import org.mule.transport.jcr.i18n.JcrMessages;
import org.mule.util.ClassUtils;

/**
 * <code>JcrConnector</code> is a transport that connects to JCR 1.0 (aka JSR 170) repositories.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrConnector extends AbstractConnector {

    public static final String PROTOCOL = "jcr";

    private Repository repository;

    private String workspaceName;

    private String username;

    private String password;

    private Integer eventTypes;

    private Boolean deep;

    private List<String> uuids;

    private List<String> nodeTypeNames;

    private Boolean noLocal;

    private String contentPayloadType;

    private final NodeTypeHandlerManager nodeTypeHandlerManager;

    /**
     * Property that defines if events coming from deeper than the specified endpoint path must be listened to.
     */
    public static final String JCR_DEEP_PROPERTY = "deep";

    /**
     * Property that defines a list of node UUIDs that will only be listened to for changes.
     */
    public static final String JCR_UUID_LIST_PROPERTY = "uuids";

    /**
     * Property that defines a combination of repository events that will only be listened to.
     */
    public static final String JCR_EVENT_TYPES_PROPERTY = "eventTypes";

    /**
     * Property that defines a list of node types that will only be listened to for changes.
     */
    public static final String JCR_NODE_TYPE_NAME_LIST_PROPERTY = "nodeTypeNames";

    /**
     * Property that defines the type of payload that a JCR <code>MuleMessage</code> will contain.
     */
    public static final String JCR_CONTENT_PAYLOAD_TYPE_PROPERTY = "contentPayloadType";

    /**
     * Property that defines if local events must be ignored.
     */
    public static final String JCR_NO_LOCAL_PROPERTY = "noLocal";

    /**
     * Property that defines a relative path to append at the end of the target item path.
     */
    public static final String JCR_PROPERTY_REL_PATH_PROPERTY = "propertyRelPath";

    /**
     * Property that defines a relative path to append after the endpoint item path.
     */
    public static final String JCR_NODE_RELPATH_PROPERTY = "nodeRelPath";

    /**
     * Property that forces the creation of a child node under the node target by the endpoint URI, instead of trying first to
     * locate an existing one.
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

    /**
     * Property that defines a repository search query statement.
     */
    public static final String JCR_QUERY_STATEMENT_PROPERTY = "queryStatement";

    /**
     * Property that defines a repository search query language.
     */
    public static final String JCR_QUERY_LANGUAGE_PROPERTY = "queryLanguage";

    public JcrConnector() {
        super();

        setDefaultEndpointValues();

        nodeTypeHandlerManager = new NodeTypeHandlerManager();
    }

    @Override
    public void doInitialise() throws InitialisationException {
        if (getRepository() == null) {
            throw new InitialisationException(JcrMessages.missingDependency("repository"), this);
        }

        if (getRepository().getDescriptor(Repository.OPTION_QUERY_SQL_SUPPORTED) == null) {
            logger.info(JcrMessages.sqlQuerySyntaxNotSupported());
        }

    }

    @Override
    public void doConnect() throws Exception {
        // NOOP
    }

    @Override
    public void doStart() throws MuleException {
        // NOOP
    }

    @Override
    public void doStop() throws MuleException {
        // NOOP
    }

    @Override
    public void doDisconnect() throws Exception {
        // NOOP
    }

    @Override
    public void doDispose() {
        // NOOP
    }

    /**
     * Will get the output stream for this type of transport. Typically this will be called only when Streaming is being used on
     * an outbound endpoint.
     */
    @Override
    public OutputStream getOutputStream(final OutboundEndpoint endpoint, final MuleMessage message) throws MuleException {

        final PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream;

        try {
            pipedOutputStream = new PipedOutputStream(pipedInputStream);
        } catch (final IOException ioe) {
            throw new ConnectorException(CoreMessages.streamingFailedForEndpoint(endpoint.toString()), this, ioe);
        }

        final MuleEvent event = RequestContext.getEvent();

        // It is essential to use a different thread for reading the piped input
        // stream. Doing a dispatch and relying on Mule's work manager to do so
        // might be a better option but this would require to force threading
        // and never execute in the same thread even if the pool is expired.
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    send(endpoint, new DefaultMuleEvent(new DefaultMuleMessage(pipedInputStream, message, getMuleContext()),
                            event));

                } catch (final DispatchException de) {
                    logger.error("Can not send streaming message!", de);
                }
            }
        });

        thread.start();

        return new CallbackOutputStream(pipedOutputStream, new JoinThreadCallback(thread));
    }

    private static final class JoinThreadCallback implements CallbackOutputStream.Callback {

        private final Thread thread;

        JoinThreadCallback(final Thread thread) {
            this.thread = thread;
        }

        public void onClose() throws Exception {
            thread.join();
        }

    }

    public Session newSession() throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Opening new JCR session.");
        }

        final Credentials credentials =
                ((getUsername() != null) && (getPassword() != null)) ? new SimpleCredentials(getUsername(),
                        getPassword().toCharArray()) : null;

        return getRepository().login(credentials, getWorkspaceName());
    }

    public void terminateSession(final Session session) {
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

    public Session validateSession(final Session session) {
        if ((session != null) && (session.isLive())) {
            return session;
        }

        logger.info("JCR session is invalid: a new one will be created.");

        final AtomicReference<Session> newSessionReference = new AtomicReference<Session>();

        try {
            getRetryPolicyTemplate().execute(new RetryCallback() {

                public void doWork(final RetryContext context) throws Exception {
                    newSessionReference.set(newSession());
                }

                public String getWorkDescription() {
                    return "Refreshing JCR session for: " + getConnectionDescription();
                }
            }, muleContext.getWorkManager());

        } catch (final Exception e) {
            throw new RuntimeException("Error when recreating a session to the JCR container!", e);
        }

        final Session newSession = newSessionReference.get();

        Validate.notNull(newSession, "The JCR has not be refreshed and is permanently invalid");

        return newSession;
    }

    private void setDefaultEndpointValues() {
        // any change here must be reflected in the documentation
        setContentPayloadType(JcrContentPayloadType.NONE.toString());
        setEventTypes(Integer.valueOf(0));
        setDeep(Boolean.FALSE);
        setNoLocal(Boolean.TRUE);
        setUuids(null);
        setNodeTypeNames(null);
    }

    public String getProtocol() {
        return PROTOCOL;
    }

    /**
     * Sets an optional list of <code>NodeTypeHandlers</code> class names.
     * 
     * @param customNodeTypeHandlers
     */
    public void setCustomNodeTypeHandlers(final List<Class<? extends NodeTypeHandler>> customNodeTypeHandlers) {

        if (customNodeTypeHandlers != null) {
            for (final Class<? extends NodeTypeHandler> customNodeTypeHandlerClass : customNodeTypeHandlers) {

                try {
                    final NodeTypeHandler handler =
                            (NodeTypeHandler) ClassUtils.instanciateClass(customNodeTypeHandlerClass, ClassUtils.NO_ARGS);

                    getNodeTypeHandlerManager().registerHandler(handler);
                } catch (final Exception e) {
                    logger.error("Can not load custom type handler: " + customNodeTypeHandlerClass.getName(), e);
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
    public void setPassword(final String password) {
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
    public void setRepository(final Repository repository) {
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
    public void setUsername(final String username) {
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
    public void setWorkspaceName(final String workspaceName) {
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
    public void setDeep(final Boolean deep) {
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
    public void setEventTypes(final Integer eventTypes) {
        this.eventTypes = eventTypes;
    }

    /**
     * @return the nodeTypeNames
     */
    List<String> getNodeTypeNames() {
        return nodeTypeNames;
    }

    /**
     * @param nodeTypeNames
     *            the nodeTypeNames to set
     */
    public void setNodeTypeNames(final String nodeTypeNames) {
        this.nodeTypeNames = JcrNamespaceHandler.split(nodeTypeNames);
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
    public void setNoLocal(final Boolean noLocal) {
        this.noLocal = noLocal;
    }

    /**
     * @return the uuid
     */
    List<String> getUuids() {
        return uuids;
    }

    /**
     * @param uuid
     *            the uuid to set
     */
    public void setUuids(final String uuids) {
        this.uuids = JcrNamespaceHandler.split(uuids);
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
    public void setContentPayloadType(final String contentPayloadType) {
        this.contentPayloadType = contentPayloadType;
    }

}
