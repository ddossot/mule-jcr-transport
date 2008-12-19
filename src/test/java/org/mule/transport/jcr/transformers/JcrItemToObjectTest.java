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
import org.mule.transformer.AbstractTransformerTestCase;
import org.mule.transport.jcr.RepositoryTestSupport;
import org.mule.transport.jcr.support.JcrUtils;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrItemToObjectTest extends AbstractTransformerTestCase {

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		RepositoryTestSupport.resetRepository();

		try {
			RepositoryTestSupport.getSession().save();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object getResultData() {
		try {
			return JcrUtils.getItemPayload(RepositoryTestSupport
					.getTestDataNode());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Transformer getRoundTripTransformer() throws Exception {
		return null;
	}

	@Override
	public Object getTestData() {
		return RepositoryTestSupport.getTestDataNode();
	}

	@Override
	public Transformer getTransformer() throws Exception {
		return new JcrItemToObject();
	}

}
