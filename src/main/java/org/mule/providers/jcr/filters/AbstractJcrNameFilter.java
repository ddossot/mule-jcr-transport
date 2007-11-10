/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr.filters;

import org.mule.umo.UMOFilter;
import org.mule.umo.UMOMessage;

/**
 * @author David Dossot (david@dossot.net)
 */
public abstract class AbstractJcrNameFilter implements UMOFilter {

	private String pattern = null;

	public AbstractJcrNameFilter() {
		super();
	}

	public boolean accept(UMOMessage message) {
		return true;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

}