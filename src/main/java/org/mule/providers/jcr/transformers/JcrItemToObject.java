/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr.transformers;

import javax.jcr.Item;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.mule.providers.jcr.JcrMessageUtils;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

/**
 * Transforms an JCR <code>Item</code> into an object that can be used as a
 * payload.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrItemToObject extends AbstractTransformer {

	public JcrItemToObject() {
		super();
		registerSourceType(Item.class);
		registerSourceType(PropertyIterator.class);
	}

	protected Object doTransform(Object src, String encoding)
			throws TransformerException {

		try {
			if (src == null) {
				return null;
			} else if (src instanceof Item) {
				return JcrMessageUtils.getItemPayload((Item) src);
			} else if (src instanceof PropertyIterator) {
				return JcrMessageUtils
						.getPropertiesPayload((PropertyIterator) src);
			} else {
				throw new IllegalArgumentException("Unsupported source: " + src);
			}

		} catch (RepositoryException re) {
			throw new TransformerException(this, re);
		}
	}
}
