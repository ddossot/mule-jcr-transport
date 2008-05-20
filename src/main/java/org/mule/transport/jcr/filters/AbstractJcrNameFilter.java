/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr.filters;

import org.mule.api.MuleMessage;
import org.mule.api.routing.filter.Filter;

/**
 * @author David Dossot (david@dossot.net)
 */
public abstract class AbstractJcrNameFilter implements Filter {

	private String pattern = null;

	public AbstractJcrNameFilter() {
		super();
	}

	public boolean accept(final MuleMessage message) {
		return true;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(final String pattern) {
		this.pattern = pattern;
	}

}