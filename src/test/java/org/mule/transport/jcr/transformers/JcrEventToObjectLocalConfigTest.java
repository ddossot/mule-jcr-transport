/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr.transformers;

import org.mule.api.transformer.Transformer;
import org.mule.transport.jcr.JcrContentPayloadType;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrEventToObjectLocalConfigTest extends JcrEventToObjectTest {
	@Override
	public Transformer getTransformer() throws Exception {
		final JcrEventToObject jcrEventToObject = new JcrEventToObject();
		jcrEventToObject.setContentPayloadType(JcrContentPayloadType.FULL
				.toString());
		return jcrEventToObject;
	}
}
