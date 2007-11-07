/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.impl.MuleMessage;
import org.mule.impl.RequestContext;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.endpoint.UMOImmutableEndpoint;

/**
 * TODO document
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageDispatcher extends AbstractMessageDispatcher {
	private final JcrConnector connector;

	public JcrMessageDispatcher(UMOImmutableEndpoint endpoint) {
		super(endpoint);
		connector = (JcrConnector) endpoint.getConnector();
	}

	public void doConnect() throws Exception {
		// NOOP
	}

	public void doDisconnect() throws Exception {
		// NOOP
	}

	public void doDispatch(UMOEvent event) throws Exception {
		/*
		 * IMPLEMENTATION NOTE: This is invoked when the endpoint is
		 * asynchronous. It should invoke the transport but not return any
		 * result. If a result is returned it should be ignorred, but if the
		 * underlying transport does have a notion of asynchronous processing,
		 * that should be invoked. This method is executed in a different thread
		 * to the request thread.
		 */

		// TODO Write the client code here to dispatch the event over this
		// transport
		throw new UnsupportedOperationException("doDispatch");
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		/*
		 * IMPLEMENTATION NOTE: Should send the event payload over the
		 * transport. If there is a response from the transport it shuold be
		 * returned from this method. The sendEvent method is called when the
		 * endpoint is running synchronously and any response returned will
		 * ultimately be passed back to the callee. This method is executed in
		 * the same thread as the request thread.
		 */

		// TODO Write the client code here to send the event over this
		// transport (or to dispatch the event to a store or repository)
		// TODO Once the event has been sent, return the result (if any)
		// wrapped in a MuleMessage object
		throw new UnsupportedOperationException("doSend");
	}

	public UMOMessage doReceive(long ignoredTimeout) throws Exception {
		Session session = connector.getSession();

		if ((session != null) && (session.isLive())) {
			Item targetItem = null;
			boolean eventOverride = false;
			UMOEvent event = RequestContext.getEvent();

			if (event != null) {
				if (logger.isDebugEnabled()) {
					// TODO i18n message
					logger.debug("Receiving from JCR with event: " + event);
				}

				String nodeUUID = (String) event.getProperty(
						JcrConnector.JCR_NODE_UUID_PROPERTY, false);

				if (nodeUUID != null) {
					eventOverride = true;

					try {
						targetItem = session.getNodeByUUID(nodeUUID);
					} catch (RepositoryException re) {
						// TODO i18n message
						logger.warn("No node was found for UUID '" + nodeUUID
								+ "', using a null payload.");
					}
				}

				// TODO check if there is jcr.nodeRelpath or jcr.propertyRelPath
				// override

				// TODO if the targeted item is a node, support filter that
				// define a propertyNamePattern
			}

			// no item was targeted by a specific event property override, hence
			// try to get one from the endpoint configuration
			if (!eventOverride) {
				if (logger.isDebugEnabled()) {
					// TODO i18n message
					logger.debug("Receiving from JCR for endpoint: "
							+ getEndpoint());
				}

				// TODO if the targeted item is a node, support filter that
				// define a propertyNamePattern

				UMOEndpointURI uri = getEndpoint().getEndpointURI();

				// TODO use specific endpoint builder
				targetItem = session.getItem("/" + uri.getHost()
						+ uri.getPath());
			}

			return new MuleMessage(targetItem != null ? JcrMessageFactory
					.getItemPayload(targetItem) : null);

		} else {
			throw new IllegalStateException("Invalid session: " + session);
		}

	}

	public void doDispose() {
		// Optional; does not need to be implemented. Delete if not required

		/*
		 * IMPLEMENTATION NOTE: Is called when the Dispatcher is being disposed
		 * and should clean up any open resources.
		 */
	}

}
