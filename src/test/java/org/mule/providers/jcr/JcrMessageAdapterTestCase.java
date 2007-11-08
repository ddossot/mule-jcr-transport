/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr;

import java.util.Arrays;
import java.util.Collection;

import org.mule.tck.providers.AbstractMessageAdapterTestCase;
import org.mule.umo.MessagingException;
import org.mule.umo.provider.UMOMessageAdapter;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageAdapterTestCase extends AbstractMessageAdapterTestCase {

	public Object getValidMessage() throws Exception {
		return getJcrMessages();
	}

	public UMOMessageAdapter createAdapter(Object payload)
			throws MessagingException {
		return new JcrMessageAdapter(payload);
	}

	public static Collection getJcrMessages() {
		return Arrays.asList(new JcrMessage[] { new JcrMessage(null, 0, null,
				null, null) });
	}

}