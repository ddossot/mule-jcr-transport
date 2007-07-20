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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrHeterogeneousCollectionMessageAdapterTestCase extends
		JcrMessageAdapterTestCase {

	public Object getInvalidMessage() {
		Collection hc = new ArrayList(JcrMessageAdapterTestCase.getJcrMessages());
		hc.add(super.getInvalidMessage());
		return hc;
	}

}
