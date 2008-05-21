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

import org.mule.api.MuleException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.transport.MessageRequester;
import org.mule.transport.AbstractMessageRequesterFactory;

/**
 * <code>JcrMessageRequesterFactory</code> creates a message requester that is
 * responsible for receiving messages from JCR repositories.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageRequesterFactory extends AbstractMessageRequesterFactory {

	@Override
	public MessageRequester create(final InboundEndpoint endpoint)
			throws MuleException {

		return new JcrMessageRequester(endpoint);
	}

}
