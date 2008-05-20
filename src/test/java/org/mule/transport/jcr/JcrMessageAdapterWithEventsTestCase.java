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

import java.util.Collections;

import javax.jcr.observation.Event;

import org.apache.jackrabbit.commons.iterator.EventIteratorAdapter;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageAdapterWithEventsTestCase extends
		JcrMessageAdapterTestCase {

	public Object getValidMessage() throws Exception {
		return new EventIteratorAdapter(Collections
				.singleton(new JcrEventTestCase.DummyEvent("/",
						Event.PROPERTY_CHANGED, "foo")));
	}

}