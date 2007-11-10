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

import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;

/**
 * Provides a Java API to JCR messages bundle.
 * 
 * @author David Dossot (david@dossot.net)
 */
public abstract class JcrMessages extends MessageFactory {

	private static final String BUNDLE_PATH = getBundlePath("jcr");

	public static Message missingDependency(String name) {
		return createMessage(BUNDLE_PATH, 0, name);
	}

	public static Message observationsNotSupported() {
		return createMessage(BUNDLE_PATH, 1);
	}

	public static Message canNotGetObservationManager(String workspaceName) {
		return createMessage(BUNDLE_PATH, 2, workspaceName);
	}

	public static Message noNodeFor(String criteria) {
		return createMessage(BUNDLE_PATH, 3, criteria);
	}

	public static Message badFilterType(Class clazz) {
		return createMessage(BUNDLE_PATH, 4, clazz);
	}

	public static Message moreThanOneNodeFor(String criteria) {
		return createMessage(BUNDLE_PATH, 5, criteria);
	}
}
