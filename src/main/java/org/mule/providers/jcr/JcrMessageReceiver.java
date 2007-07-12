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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.providers.jcr.i18n.JcrMessages;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.LifecycleException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

/**
 * Registers a JCR <code>javax.jcr.observation.EventListener</code> to the
 * <code>javax.jcr.observation.ObservationManager</code> of the repository.
 */
public final class JcrMessageReceiver extends AbstractMessageReceiver implements
		EventListener {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private final JcrConnector jcrConnector;

	private final Integer eventTypes;

	private final String absPath;

	private final Boolean deep;

	private final List uuid;

	private final List nodeTypeName;

	private final Boolean noLocal;

	private final JcrContentPayloadType contentPayloadType;

	private ObservationManager observationManager;

	public JcrMessageReceiver(UMOConnector connector, UMOComponent component,
			UMOEndpoint endpoint) throws InitialisationException {

		super(connector, component, endpoint);

		jcrConnector = (JcrConnector) getConnector();

		if (logger.isDebugEnabled()) {
			logger.debug("Initializing for: " + endpoint);
		}

		// Future JCR version will offer a standard way to get a repository
		// instance, at that time host/port will be used for what they are on
		// the endpoint because it will become possible to specify a full URL to
		// a repository
		absPath = "/" + endpoint.getEndpointURI().getHost()
				+ endpoint.getEndpointURI().getPath();

		eventTypes = (Integer) new IntegerConverter(jcrConnector
				.getEventTypes()).convert(Integer.class, endpoint
				.getProperty("eventTypes"));

		deep = (Boolean) new BooleanConverter(jcrConnector.isDeep()).convert(
				Boolean.class, endpoint.getProperty("deep"));

		Object uuidProperty = endpoint.getProperty("uuid");

		if (uuidProperty == null) {
			uuid = jcrConnector.getUuid();
		} else {
			uuid = (List) uuidProperty;
		}

		Object nodeTypeNameProperty = (List) endpoint
				.getProperty("nodeTypeName");

		if (nodeTypeNameProperty == null) {
			nodeTypeName = jcrConnector.getNodeTypeName();
		} else {
			nodeTypeName = (List) nodeTypeNameProperty;
		}

		noLocal = (Boolean) new BooleanConverter(jcrConnector.isNoLocal())
				.convert(Boolean.class, endpoint.getProperty("noLocal"));

		String contentPayloadTypeProperty = (String) endpoint
				.getProperty("contentPayloadType");

		if (contentPayloadTypeProperty == null) {
			contentPayloadTypeProperty = jcrConnector.getContentPayloadType();
		}

		try {
			contentPayloadType = JcrContentPayloadType
					.fromString(contentPayloadTypeProperty);
		} catch (IllegalArgumentException iae) {
			throw new InitialisationException(iae, this);
		}
	}

	public void doConnect() throws ConnectException {
		if (jcrConnector.getRepository().getDescriptor(
				Repository.OPTION_OBSERVATION_SUPPORTED) == null) {
			throw new ConnectException(JcrMessages.observationsNotSupported(),
					this);
		}

		try {
			observationManager = jcrConnector.getSession().getWorkspace()
					.getObservationManager();
		} catch (Exception e) {
			throw new ConnectException(JcrMessages
					.canNotGetObservationManager(jcrConnector
							.getWorkspaceName()), e, this);
		}
	}

	public void doStart() throws UMOException {
		try {
			observationManager.addEventListener(this, eventTypes.intValue(),
					absPath, deep.booleanValue(), uuid == null ? null
							: (String[]) uuid.toArray(EMPTY_STRING_ARRAY),
					nodeTypeName == null ? null : (String[]) nodeTypeName
							.toArray(EMPTY_STRING_ARRAY), noLocal
							.booleanValue());

			if (logger.isInfoEnabled()) {
				logger.info("Observing JCR for events of types: " + eventTypes
						+ " - at: " + absPath + " - deep: " + deep
						+ " - uuid: " + uuid + " - nodeTypeName: "
						+ nodeTypeName + " - noLocal: " + noLocal
						+ " - contentPayloadType: " + contentPayloadType);
			}

		} catch (RepositoryException re) {
			throw new LifecycleException(re, this);
		}
	}

	public void doStop() throws UMOException {
		try {
			observationManager.removeEventListener(this);
		} catch (RepositoryException re) {
			throw new LifecycleException(re, this);
		}

	}

	public void doDisconnect() throws ConnectException {
		// NOOP
	}

	public void doDispose() {
		observationManager = null;
	}

	public void onEvent(EventIterator eventIterator) {
		if (logger.isDebugEnabled()) {
			logger.debug("JCR events received");
		}

		List eventList = new ArrayList();

		while (eventIterator.hasNext()) {
			try {

				eventList.add(JcrEvent.newInstance(eventIterator.nextEvent(),
						jcrConnector.getSession(), contentPayloadType));

			} catch (RepositoryException re) {
				logger.error("Can not process JCR event", re);
			}
		}

		try {
			UMOMessageAdapter adapter = jcrConnector
					.getMessageAdapter(eventList);

			routeMessage(new MuleMessage(adapter));

		} catch (MessagingException me) {
			handleException(me);
		} catch (UMOException umoe) {
			handleException(umoe);
		}
	}
}
