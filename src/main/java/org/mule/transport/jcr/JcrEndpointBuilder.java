/*
 * $Id: EndpointBuilder.vm 11571 2008-04-12 00:22:07Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import java.net.URI;
import java.util.Properties;

import org.mule.api.endpoint.EndpointException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.MalformedEndpointException;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.endpoint.AbstractEndpointBuilder;

/**
 * <code>JcrEndpointBuilder</code> TODO Document
 */
public class JcrEndpointBuilder extends AbstractEndpointBuilder {
	protected void setEndpoint(final URI uri, final Properties props)
			throws MalformedEndpointException {
		/*
		 * IMPLEMENTATION NOTE: This method should set the this.address variable
		 * to the endpoint value to be retruned when calling
		 * MuleEndpointURI.getAddress(). Mule uses this portion to pass to the
		 * underlying technology
		 */

		// TODO extract the endpoint config from the uri
		throw new UnsupportedOperationException("setEndpoint");
	}

	@Override
	protected InboundEndpoint doBuildInboundEndpoint()
			throws EndpointException, InitialisationException {
		// TODO implement me
		throw new UnsupportedOperationException("doBuildInboundEndpoint");
	}

	@Override
	protected OutboundEndpoint doBuildOutboundEndpoint()
			throws EndpointException, InitialisationException {
		// TODO implement me
		throw new UnsupportedOperationException("doBuildInboundEndpoint");
	}

}
