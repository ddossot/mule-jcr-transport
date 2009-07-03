/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import java.util.Collections;
import java.util.List;

import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.endpoint.URIBuilder;
import org.mule.util.StringUtils;

/**
 * @author David Dossot
 */
public class JcrMessageReceiverOverridesTestCase extends JcrMessageReceiverTestCase {

    private static final List<String> UUID_LIST = Collections.singletonList("uuid_01");

    private static final List<String> NODE_TYPE_NAME_LIST = Collections.singletonList("node_type_01");

    @SuppressWarnings("unchecked")
    @Override
    public InboundEndpoint getEndpoint() throws Exception {
        final EndpointBuilder builder =
                new EndpointURIEndpointBuilder(
                        new URIBuilder(
                                "jcr://path/to/observedNode?contentPayloadType=full&eventTypes=5&deep=true&noLocal=false",
                                muleContext), muleContext);

        builder.setConnector(JcrConnectorTestCase.newJcrConnector());

        endpoint = muleContext.getRegistry().lookupEndpointFactory().getInboundEndpoint(builder);

        endpoint.getProperties().put("uuids", StringUtils.join(UUID_LIST, ' '));

        endpoint.getProperties().put("nodeTypeNames", StringUtils.join(NODE_TYPE_NAME_LIST, ' '));

        return endpoint;
    }

    public void testReceiverProperties() throws Exception {
        final JcrMessageReceiver messageReceiver = (JcrMessageReceiver) getMessageReceiver();

        assertEquals("/path/to/observedNode", messageReceiver.getAbsPath());

        assertEquals(JcrContentPayloadType.FULL, messageReceiver.getContentPayloadType());

        assertEquals(new Integer(5), messageReceiver.getEventTypes());

        assertEquals(Boolean.TRUE, messageReceiver.isDeep());

        assertEquals(UUID_LIST, messageReceiver.getUuids());

        assertEquals(NODE_TYPE_NAME_LIST, messageReceiver.getNodeTypeNames());

        assertEquals(Boolean.FALSE, messageReceiver.isNoLocal());
    }

}
