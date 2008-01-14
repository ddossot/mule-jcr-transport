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

import java.io.ByteArrayInputStream;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageAdapterWithStreamTestCase extends
		JcrMessageAdapterTestCase {

	public Object getValidMessage() throws Exception {
		return new ByteArrayInputStream("foo".getBytes());
	}

}