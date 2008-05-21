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

import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.transport.AbstractMessageRequester;

/**
 * <code>JcrMessageRequester</code> is responsible for receiving messages from
 * JCR repositories.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageRequester extends AbstractMessageRequester {

	private final JcrConnector jcrConnector;

	public JcrMessageRequester(final InboundEndpoint endpoint) {
		super(endpoint);

		jcrConnector = (JcrConnector) endpoint.getConnector();
	}

	@Override
	protected void doConnect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDisconnect() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected MuleMessage doRequest(final long timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
