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

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.InboundEndpoint;
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
		final EndpointBuilder builder = new EndpointURIEndpointBuilder(
				new URIBuilder("jcr://path/to/observedNode"), muleContext);

		builder.setConnector(JcrConnectorTestCase.newJcrConnector());
		endpoint = muleContext.getRegistry().lookupEndpointFactory()
				.getInboundEndpoint(builder);

		return endpoint;
	}

}
