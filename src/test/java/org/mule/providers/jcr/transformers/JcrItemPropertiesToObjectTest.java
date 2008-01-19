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

import javax.jcr.RepositoryException;

import org.mule.providers.jcr.JcrUtils;
import org.mule.providers.jcr.RepositoryTestSupport;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrItemPropertiesToObjectTest extends JcrItemToObjectTest {

	public Object getResultData() {
		try {
			return JcrUtils.getPropertiesPayload(RepositoryTestSupport
					.getTestDataNode().getProperties());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object getTestData() {
		try {
			return RepositoryTestSupport.getTestDataNode().getProperties();
		} catch (RepositoryException re) {
			throw new RuntimeException(re);
		}
	}

}
