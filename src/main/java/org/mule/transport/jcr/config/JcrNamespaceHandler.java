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

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.specific.FilterDefinitionParser;
import org.mule.config.spring.parsers.specific.TransformerDefinitionParser;
import org.mule.transport.jcr.JcrConnector;
import org.mule.transport.jcr.filters.JcrNodeNameFilter;
import org.mule.transport.jcr.filters.JcrPropertyNameFilter;
import org.mule.transport.jcr.transformers.JcrEventToObject;
import org.mule.transport.jcr.transformers.JcrItemToObject;
import org.mule.util.StringUtils;

/**
 * Registers a Bean Definition Parser for handling <code><jcr:connector></code> elements.
 */
public class JcrNamespaceHandler extends AbstractMuleNamespaceHandler {
    private static final String[] JCR_ATTRIBUTES = new String[] { "path" };

    public void init() {
        registerStandardTransportEndpoints(JcrConnector.PROTOCOL, JCR_ATTRIBUTES);
        registerConnectorDefinitionParser(JcrConnector.class);
        registerBeanDefinitionParser("node-name-filter", new FilterDefinitionParser(JcrNodeNameFilter.class));
        registerBeanDefinitionParser("property-name-filter", new FilterDefinitionParser(JcrPropertyNameFilter.class));
        registerBeanDefinitionParser("event-to-object-transformer", new TransformerDefinitionParser(JcrEventToObject.class));
        registerBeanDefinitionParser("item-to-object-transformer", new TransformerDefinitionParser(JcrItemToObject.class));
    }

    /**
     * Supports configuration that uses attributes containing lists of whitespace separated values.
     */
    public static List<String> split(final String values) {
        return StringUtils.isNotBlank(values) ? Arrays.asList(StringUtils.split(values)) : null;
    }
}