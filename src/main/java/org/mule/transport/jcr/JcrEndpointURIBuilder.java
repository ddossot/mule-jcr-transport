/*
 * $Id: EndpointBuilder.vm 11571 2008-04-12 00:22:07Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import java.net.URI;
import java.util.Properties;

import org.mule.api.endpoint.EndpointException;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.MalformedEndpointException;
import org.mule.endpoint.AbstractEndpointURIBuilder;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.util.StringUtils;

/**
 * <code>JcrEndpointURIBuilder</code> is a builder that is specific to the JCR
 * transport.
 */
public class JcrEndpointURIBuilder extends AbstractEndpointURIBuilder {
	/**
	 * Helper method for safely building a JCR endpoint URI from a node path,
	 * which can contains the [ and ] reserved characters.
	 * 
	 * @param path
	 *            a node path.
	 * 
	 * @return a new EndpointURI.
	 * 
	 * @throws EndpointException
	 *             thrown in case the path can not be transformed into a valid
	 *             EndpointURI.
	 */
	public static EndpointURI newJcrEndpointURI(final String path)
			throws EndpointException {

		return new MuleEndpointURI("jcr://"
				+ path.replaceAll("\\[", "%5B").replaceAll("\\]", "%5D"));
	}

	@Override
	protected void setEndpoint(final URI uri, final Properties props)
			throws MalformedEndpointException {
		address = "/"
				+ StringUtils.stripStart(StringUtils.defaultString(uri
						.getHost())
						+ StringUtils.defaultString(uri.getPath()), "/");
		;
	}

}
