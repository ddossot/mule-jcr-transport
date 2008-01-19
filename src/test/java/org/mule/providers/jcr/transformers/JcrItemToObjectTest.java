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

import org.mule.providers.jcr.JcrUtils;
import org.mule.providers.jcr.RepositoryTestSupport;
import org.mule.tck.AbstractTransformerTestCase;
import org.mule.umo.transformer.UMOTransformer;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrItemToObjectTest extends AbstractTransformerTestCase {

	public Object getResultData() {
		try {
			return JcrUtils.getItemPayload(RepositoryTestSupport
					.getTestDataNode());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public UMOTransformer getRoundTripTransformer() throws Exception {
		return null;
	}

	public Object getTestData() {
		RepositoryTestSupport.resetRepository();

		try {
			RepositoryTestSupport.getSession().save();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return RepositoryTestSupport.getTestDataNode();
	}

	public UMOTransformer getTransformer() throws Exception {
		return new JcrItemToObject();
	}

}
