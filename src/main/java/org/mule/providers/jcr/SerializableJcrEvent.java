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

import java.io.Serializable;

/**
 * Defines a serializable, disconnected and fully resolved JCR event
 * representation. JCR events are often RMI stubs hence not fitted to be carried
 * in messages payloads. This class also translates event types from numeric to
 * plain English and supports the notion of event content, that is mising in the
 * JCR event.
 * 
 * @author David Dossot (david@dossot.net)
 */
public interface SerializableJcrEvent extends Serializable {

	/**
	 * @return the content
	 */
	String getContent();

	/**
	 * @return the path
	 */
	String getPath();

	/**
	 * @return the type
	 */
	String getType();

	/**
	 * @return the userID
	 */
	String getUserID();

}