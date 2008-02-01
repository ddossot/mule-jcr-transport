/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr;

import java.net.URI;
import java.util.Properties;

import org.mule.impl.endpoint.AbstractEndpointBuilder;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.util.StringUtils;

/**
 * JCR endpoints do not follow the regular host/resource path, hence need this
 * particular builder.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrEndpointBuilder extends AbstractEndpointBuilder {

    /**
     * Helper method for safely building a JCR endpoint URI from a node path,
     * which can contains the [ and ] reserved characters.
     * 
     * @param path
     *            a node path.
     * 
     * @return a new UMOEndpointURI.
     * 
     * @throws MalformedEndpointException
     *             thrown in case the path can not be transformed into a valid
     *             UMOEndpointURI.
     */
    public static UMOEndpointURI newJcrEndpointURI(String path)
            throws MalformedEndpointException {

        return new MuleEndpointURI("jcr://"
                + path.replaceAll("\\[", "%5B").replaceAll("\\]", "%5D"));
    }

    protected void setEndpoint(URI uri, Properties props)
            throws MalformedEndpointException {

        address =
                "/"
                        + StringUtils.stripStart(
                                StringUtils.defaultString(uri.getHost())
                                        + StringUtils.defaultString(uri.getPath()),
                                "/");

    }

}
