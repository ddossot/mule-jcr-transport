/*
 * $Id: MessageReceiverTestCase.vm 11571 2008-04-12 00:22:07Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import org.mule.api.MuleContext;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.routing.filter.Filter;
import org.mule.api.service.Service;
import org.mule.api.transport.MessageReceiver;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.endpoint.URIBuilder;
import org.mule.transport.AbstractMessageReceiverTestCase;

import com.mockobjects.dynamic.Mock;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageReceiverTestCase extends AbstractMessageReceiverTestCase {

	@Override
	public MessageReceiver getMessageReceiver() throws Exception {
		final Mock mockService = new Mock(Service.class);
		mockService.expectAndReturn("getResponseRouter", null);

		return new JcrMessageReceiver(endpoint.getConnector(),
				(Service) mockService.proxy(), endpoint);
	}

	@Override
	public InboundEndpoint getEndpoint() throws Exception {
		return newInboundEndpoint(muleContext, "jcr://path/to/observedNode");
	}

	static InboundEndpoint newInboundEndpoint(final MuleContext muleContext,
			final String address) throws Exception {
		return newInboundEndpoint(muleContext, address, null);
	}

	static InboundEndpoint newInboundEndpoint(final MuleContext muleContext,
			final String address, final Filter filter) throws Exception {

		final EndpointBuilder builder = new EndpointURIEndpointBuilder(
				new URIBuilder(address), muleContext);

		if (filter != null) {
			builder.setFilter(filter);
		}

		final JcrConnector jcrConnector = JcrConnectorTestCase
				.newJcrConnector();

		jcrConnector.setMuleContext(muleContext);
		jcrConnector.initialise();
		builder.setConnector(jcrConnector);

		return muleContext.getRegistry().lookupEndpointFactory()
				.getInboundEndpoint(builder);
	}

}
