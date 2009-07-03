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

import org.mule.api.MuleException;
import org.mule.api.transport.MessageAdapter;
import org.mule.transport.AbstractMessageAdapterTestCase;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageAdapterTestCase extends AbstractMessageAdapterTestCase {

    @Override
    public Object getValidMessage() throws Exception {
        return "foo";
    }

    @Override
    public MessageAdapter createAdapter(final Object payload) throws MuleException {
        return new JcrMessageAdapter(payload);
    }

}
