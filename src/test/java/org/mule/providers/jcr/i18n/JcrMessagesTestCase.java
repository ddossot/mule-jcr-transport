/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr.i18n;

import junit.framework.TestCase;

/**
 * @author David Dossot
 */
public class JcrMessagesTestCase extends TestCase {
	public void testMessages() {
		assertTrue(JcrMessages.canNotGetObservationManager("foo").getMessage()
				.indexOf("foo") >= 0);

		assertTrue(JcrMessages.missingDependency("bar").getMessage().indexOf(
				"bar") >= 0);

		assertNotNull(JcrMessages.observationsNotSupported());
	}
}
