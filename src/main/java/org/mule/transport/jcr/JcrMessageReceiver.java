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

import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.mule.DefaultMuleMessage;
import org.mule.api.MessagingException;
import org.mule.api.MuleException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.lifecycle.LifecycleException;
import org.mule.api.service.Service;
import org.mule.api.transport.Connector;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.transport.ConnectException;
import org.mule.transport.jcr.i18n.JcrMessages;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicReference;

/**
 * Registers a JCR <code>javax.jcr.observation.EventListener</code> to the
 * <code>javax.jcr.observation.ObservationManager</code> of the repository.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrMessageReceiver extends AbstractMessageReceiver implements
		EventListener {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private final JcrConnector jcrConnector;

	private final Integer eventTypes;

	private final String absPath;

	private final Boolean deep;

	private final List uuids;

	private final List nodeTypeNames;

	private final Boolean noLocal;

	private final JcrContentPayloadType contentPayloadType;

	private ObservationManager observationManager;

	private Session receiverSession;

	private static final AtomicReference jcrMessageReceiverContext = new AtomicReference();

	public static JcrMessageReceiverContext getJcrMessageReceiverContext() {
		return (JcrMessageReceiverContext) jcrMessageReceiverContext.get();
	}

	public static void setJcrMessageReceiverContext(
			final JcrMessageReceiverContext context) {
		jcrMessageReceiverContext.set(context);
	}

	public JcrMessageReceiver(final Connector connector, final Service service,
			final InboundEndpoint endpoint) throws CreateException {

		super(connector, service, endpoint);

		jcrConnector = (JcrConnector) getConnector();

		if (logger.isDebugEnabled()) {
			logger.debug("Initializing for: " + endpoint);
		}

		// Future JCR version will offer a standard way to get a repository
		// instance, at that time host/port will be used for what they are on
		// the endpoint because it will become possible to specify a full URL to
		// a repository

		absPath = endpoint.getEndpointURI().getAddress();

		eventTypes = (Integer) new IntegerConverter(jcrConnector
				.getEventTypes()).convert(Integer.class, endpoint
				.getProperty(JcrConnector.JCR_EVENT_TYPES_PROPERTY));

		deep = (Boolean) new BooleanConverter(jcrConnector.isDeep()).convert(
				Boolean.class, endpoint
						.getProperty(JcrConnector.JCR_DEEP_PROPERTY));

		final String uuidProperty = (String) endpoint
				.getProperty(JcrConnector.JCR_UUID_LIST_PROPERTY);

		if (uuidProperty == null) {
			uuids = jcrConnector.getUuids();
		} else {
			uuids = JcrConnector.getTokenizedValues(uuidProperty);
		}

		final String nodeTypeNameProperty = (String) endpoint
				.getProperty(JcrConnector.JCR_NODE_TYPE_NAME_LIST_PROPERTY);

		if (nodeTypeNameProperty == null) {
			nodeTypeNames = jcrConnector.getNodeTypeNames();
		} else {
			nodeTypeNames = JcrConnector
					.getTokenizedValues(nodeTypeNameProperty);
		}

		noLocal = (Boolean) new BooleanConverter(jcrConnector.isNoLocal())
				.convert(Boolean.class, endpoint
						.getProperty(JcrConnector.JCR_NO_LOCAL_PROPERTY));

		String contentPayloadTypeProperty = (String) endpoint
				.getProperty(JcrConnector.JCR_CONTENT_PAYLOAD_TYPE_PROPERTY);

		if (contentPayloadTypeProperty == null) {
			contentPayloadTypeProperty = jcrConnector.getContentPayloadType();
		}

		try {
			contentPayloadType = JcrContentPayloadType
					.fromString(contentPayloadTypeProperty);

		} catch (final IllegalArgumentException iae) {
			throw new CreateException(iae, this);
		}
	}

	@Override
	public void doConnect() throws ConnectException {
		if (jcrConnector.getRepository().getDescriptor(
				Repository.OPTION_OBSERVATION_SUPPORTED) == null) {
			throw new ConnectException(JcrMessages.observationsNotSupported(),
					this);
		}

		try {
			receiverSession = jcrConnector.newSession();
			observationManager = receiverSession.getWorkspace()
					.getObservationManager();

			setJcrMessageReceiverContext(new JcrMessageReceiverContext() {
				public JcrContentPayloadType getContentPayloadType() {
					return contentPayloadType;
				}

				public Session getObservingSession() {
					return receiverSession;
				}
			});

		} catch (final Exception e) {
			throw new ConnectException(JcrMessages
					.canNotGetObservationManager(jcrConnector
							.getWorkspaceName()), e, this);
		}
	}

	@Override
	public void doStart() throws MuleException {
		try {
			observationManager.addEventListener(this, eventTypes.intValue(),
					absPath, deep.booleanValue(), uuids == null ? null
							: (String[]) uuids.toArray(EMPTY_STRING_ARRAY),
					nodeTypeNames == null ? null : (String[]) nodeTypeNames
							.toArray(EMPTY_STRING_ARRAY), noLocal
							.booleanValue());

			if (logger.isInfoEnabled()) {
				logger.info("Observing JCR for events of types: " + eventTypes
						+ " - at: " + absPath + " - deep: " + deep
						+ " - uuid: " + uuids + " - nodeTypeName: "
						+ nodeTypeNames + " - noLocal: " + noLocal
						+ " - contentPayloadType: " + contentPayloadType);
			}

		} catch (final RepositoryException re) {
			throw new LifecycleException(re, this);
		}
	}

	@Override
	public void doStop() throws MuleException {
		try {
			observationManager.removeEventListener(this);
		} catch (final RepositoryException re) {
			throw new LifecycleException(re, this);
		}

	}

	@Override
	public void doDisconnect() throws ConnectException {
		jcrConnector.terminateSession(receiverSession);

		receiverSession = null;
	}

	@Override
	public void doDispose() {
		observationManager = null;
	}

	public void onEvent(final EventIterator eventIterator) {
		if (logger.isDebugEnabled()) {
			logger.debug("JCR events received");
		}

		try {
			routeMessage(new DefaultMuleMessage(jcrConnector
					.getMessageAdapter(eventIterator)));

		} catch (final MessagingException me) {
			handleException(me);
		} catch (final MuleException mue) {
			handleException(mue);
		}
	}

	/**
	 * @return the absPath
	 */
	String getAbsPath() {
		return absPath;
	}

	/**
	 * @return the contentPayloadType
	 */
	JcrContentPayloadType getContentPayloadType() {
		return contentPayloadType;
	}

	/**
	 * @return the deep
	 */
	Boolean isDeep() {
		return deep;
	}

	/**
	 * @return the eventTypes
	 */
	Integer getEventTypes() {
		return eventTypes;
	}

	/**
	 * @return the nodeTypeName
	 */
	List getNodeTypeNames() {
		return nodeTypeNames;
	}

	/**
	 * @return the noLocal
	 */
	Boolean isNoLocal() {
		return noLocal;
	}

	/**
	 * @return the uuid
	 */
	List getUuids() {
		return uuids;
	}

}
