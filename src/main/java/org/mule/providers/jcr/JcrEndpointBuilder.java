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
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.util.StringUtils;

/**
 * JCR endpoints do not follow the regular host/resource path, hence need this
 * particular builder.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrEndpointBuilder extends AbstractEndpointBuilder {

    protected void setEndpoint(URI uri, Properties props)
            throws MalformedEndpointException {

        address = "/"
            + StringUtils.stripStart(StringUtils.defaultString(uri.getHost())
                + StringUtils.defaultString(uri.getPath()), "/");

    }

}
