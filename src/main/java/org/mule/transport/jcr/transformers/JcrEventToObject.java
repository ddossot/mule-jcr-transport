/*
 * $Id: InboundTransformer.vm 10787 2008-02-12 18:51:50Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr.transformers;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageAwareTransformer;

/**
 * <code>JcrEventToObject</code> TODO Document
 */
public class JcrEventToObject extends AbstractMessageAwareTransformer {

	/*
	 * For general guidelines on writing transports see
	 * http://mule.mulesource.org/display/MULE/Writing+Transports
	 */

	public JcrEventToObject() {
		/*
		 * IMPLEMENTATION NOTE: Here you can set default types that the
		 * transformer will accept at runtime. Mule will then validate the
		 * transformer at runtime. You can register one or more source types.
		 * eg.
		 * 
		 * registerSourceType(XXX.class.getName());
		 */

		/*
		 * IMPLEMENTATION NOTE: It's good practice to set the expected return
		 * type for this transformer here This helps Mule validate event flows
		 * and Transformer chains
		 * 
		 * setReturnClass(YYY.class);
		 */

	}

	@Override
	public Object transform(final MuleMessage arg0, final String arg1)
			throws TransformerException {
		/*
		 * IMPLEMENTATION NOTE: If you only have a single source type registered
		 * you can cast the 'src' object directly to that type, otherwise you
		 * need to check the instance type of 'src' for each registered source
		 * type
		 */

		// TODO Transformer the source object here. You have access to the
		// Current message (including //headers and attachments from the
		// context.getMessage())
		// Make sure you return a transfromed object that matches the
		// returnClass type
		throw new UnsupportedOperationException("transform");
	}

}
