/*
 * $Id: NamespaceHandler.vm 10787 2008-02-12 18:51:50Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.jcr.config;

import java.util.Arrays;
import java.util.List;

import org.mule.config.spring.parsers.generic.OrphanDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.TransportGlobalEndpointDefinitionParser;
import org.mule.transport.jcr.JcrConnector;
import org.mule.util.StringUtils;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Registers a Bean Definition Parser for handling <code><jcr:connector></code>
 * elements.
 * 
 */
public class JcrNamespaceHandler extends NamespaceHandlerSupport {
    public void init() {
        registerBeanDefinitionParser("connector", new OrphanDefinitionParser(
                JcrConnector.class, true));

        registerBeanDefinitionParser("endpoint",
                new TransportGlobalEndpointDefinitionParser("jcr",
                        new String[] { "path" }));

        // TODO add support for inbound and outbound
    }

    /**
     * Supports configuration that uses attributes containing lists of
     * whitespace separated values.
     */
    public static List split(final String values) {
        return StringUtils.isNotBlank(values) ? Arrays.asList(StringUtils.split(values))
                : null;
    }
}