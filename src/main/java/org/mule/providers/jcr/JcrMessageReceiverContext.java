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

import javax.jcr.Session;

/**
 * A holder for the context a JCR <code>EventIterator</code> needs.
 * 
 * @author David Dossot (david@dossot.net)
 */
public interface JcrMessageReceiverContext {

	JcrContentPayloadType getContentPayloadType();

	Session getObservingSession();

}
