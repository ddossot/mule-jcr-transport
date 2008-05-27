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

import org.mule.config.spring.parsers.collection.ChildListEntryDefinitionParser;
import org.mule.config.spring.parsers.generic.OrphanDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.TransportGlobalEndpointDefinitionParser;
import org.mule.transport.jcr.JcrConnector;
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

		registerBeanDefinitionParser("uuid",
				new ChildListEntryDefinitionParser("uuids", "value"));

		registerBeanDefinitionParser("nodeType",
				new ChildListEntryDefinitionParser("nodeTypeNames", "name"));

		registerBeanDefinitionParser("endpoint",
				new TransportGlobalEndpointDefinitionParser("jcr",
						new String[] { "path" }));

		// TODO add support for inbound and outbound
	}
}