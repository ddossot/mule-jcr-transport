/*
 * $Id: MessageDispatcherFactory.vm 10961 2008-02-22 19:01:02Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import org.mule.transport.AbstractMessageDispatcherFactory;
import org.mule.api.MuleException;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.MessageDispatcher;

/**
 * <code>JcrMessageDispatcherFactory</code> Todo document
 */

public class JcrMessageDispatcherFactory extends AbstractMessageDispatcherFactory
{

    /* For general guidelines on writing transports see
       http://mule.mulesource.org/display/MULE/Writing+Transports */

    public MessageDispatcher create(OutboundEndpoint endpoint) throws MuleException
    {
        return new JcrMessageDispatcher(endpoint);
    }

}
