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

import javax.jcr.Item;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractDiscoverableTransformer;
import org.mule.transport.jcr.support.JcrPropertyUtils;
import org.mule.transport.jcr.support.JcrNodeUtils;

/**
 * Transforms a JCR <code>Item</code> or <code>PropertyIterator</code> into
 * an object that can be used as a payload.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrItemToObject extends AbstractDiscoverableTransformer {

	public JcrItemToObject() {
		super();
		registerSourceType(Item.class);
		registerSourceType(PropertyIterator.class);
	}

	@Override
	protected Object doTransform(final Object src, final String encoding)
			throws TransformerException {

		try {
			if (src instanceof Item) {
				return JcrNodeUtils.getItemPayload((Item) src);
			} else if (src instanceof PropertyIterator) {
				return JcrPropertyUtils.getPropertiesPayload((PropertyIterator) src);
			} else {
				throw new IllegalArgumentException("Unsupported source: " + src);
			}

		} catch (final RepositoryException re) {
			throw new TransformerException(this, re);
		}
	}
}
