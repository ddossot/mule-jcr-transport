/*
 * $Id: MessageAdapterTestCase.vm 10787 2008-02-12 18:51:50Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import org.mule.transport.AbstractMessageAdapterTestCase;
import org.mule.api.transport.MessageAdapter;
import org.mule.api.MessagingException;

public class JcrMessageAdapterTestCase extends AbstractMessageAdapterTestCase
{

    /* For general guidelines on writing transports see
       http://mule.mulesource.org/display/MULE/Writing+Transports */

    public Object getValidMessage() throws Exception
    {
        // TODO Create a valid message for your transport
        throw new UnsupportedOperationException("getValidMessage");
    }

    public MessageAdapter createAdapter(Object payload) throws MessagingException
    {
        return new JcrMessageAdapter(payload);
    }

}
