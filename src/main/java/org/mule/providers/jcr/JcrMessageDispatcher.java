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

import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.mule.impl.MuleMessage;
import org.mule.impl.RequestContext;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.jcr.filters.JcrPropertyNameFilter;
import org.mule.providers.jcr.i18n.JcrMessages;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOImmutableEndpoint;

/**
 * A dispatcher for reading and writing in a JCR container.
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

	public void doDispose() {
		// NOOP
	}

	public void doDispatch(UMOEvent event) throws Exception {
		doSend(event);
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
					logger.debug("Receiving from JCR with event: " + event);
				}

				String nodeUUID = (String) event.getProperty(
						JcrConnector.JCR_NODE_UUID_PROPERTY, false);

				if (nodeUUID != null) {
					eventOverride = true;

					try {
						targetItem = session.getNodeByUUID(nodeUUID);
					} catch (RepositoryException re) {
						logger.warn(JcrMessages.noNodeForUUID(nodeUUID)
								.getMessage());
					}
				}

				// TODO check if there is jcr.nodeRelpath or jcr.propertyRelPath
				// override

			}

			// no item was targeted by a specific event property override, hence
			// try to get one from the endpoint configuration
			if (!eventOverride) {
				if (logger.isDebugEnabled()) {
					logger.debug("Receiving from JCR for endpoint: "
							+ getEndpoint());
				}

				targetItem = session.getItem(endpoint.getEndpointURI()
						.getAddress());
			}

			Object payload = null;
			UMOFilter filter = getEndpoint().getFilter();

			if (targetItem != null) {
				if ((targetItem.isNode()) && (filter != null)
						&& (filter instanceof JcrPropertyNameFilter)) {

					Map propertiesPayload = JcrMessageUtils
							.getPropertiesPayload(((Node) targetItem)
									.getProperties(((JcrPropertyNameFilter) filter)
											.getPattern()));

					// if the map contains only one property, because we have
					// applied a filter, we assume the intention was to get a
					// single property value
					if ((propertiesPayload != null)
							&& (propertiesPayload.size() == 1)) {
						payload = propertiesPayload.values().iterator().next();
					} else {
						payload = propertiesPayload;
					}
				} else {
					payload = JcrMessageUtils.getItemPayload(targetItem);
				}
			}

			return new MuleMessage(connector.getMessageAdapter(payload));
		} else {
			throw new IllegalStateException("Invalid session: " + session);
		}

	}
}
