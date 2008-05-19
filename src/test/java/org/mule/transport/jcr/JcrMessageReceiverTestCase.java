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

import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.service.Service;
import org.mule.api.transport.MessageReceiver;
import org.mule.transport.AbstractMessageReceiverTestCase;

import com.mockobjects.dynamic.Mock;

public class JcrMessageReceiverTestCase extends AbstractMessageReceiverTestCase {

	/*
	 * For general guidelines on writing transports see
	 * http://mule.mulesource.org/display/MULE/Writing+Transports
	 */

	@Override
	public MessageReceiver getMessageReceiver() throws Exception {
		final Mock mockService = new Mock(Service.class);
		mockService.expectAndReturn("getResponseTransformer", null);
		return new JcrMessageReceiver(endpoint.getConnector(),
				(Service) mockService.proxy(), endpoint);
	}

	@Override
	public InboundEndpoint getEndpoint() throws Exception {
		// TODO return a valid endpoint i.e.
		// return new MuleEndpoint("tcp://localhost:1234", true)
		throw new UnsupportedOperationException("getEndpoint");
	}

}
