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

import javax.jcr.observation.Event;

import junit.framework.TestCase;

import org.mule.MuleManager;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMODescriptor;
import org.mule.umo.UMOEvent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.routing.UMOInboundRouterCollection;

import com.mockobjects.constraint.IsInstanceOf;
import com.mockobjects.dynamic.Mock;

/**
 * @author David Dossot
 */
public class JcrMessageReceiverLifeCycleTestCase extends TestCase {

	public void testLifeCyle() throws Exception {
		Mock mockComponent = new Mock(UMOComponent.class);
		Mock mockDescriptor = new Mock(UMODescriptor.class);
		Mock mockInboundRouter = new Mock(UMOInboundRouterCollection.class);

		mockInboundRouter.expect("route", new IsInstanceOf(UMOEvent.class));

		mockDescriptor.expectAndReturn("getInboundRouter", mockInboundRouter
				.proxy());

		mockComponent.expectAndReturn("getDescriptor", mockDescriptor.proxy());

		MuleManager.getInstance().registerConnector(new JcrConnector());

		UMOEndpoint endpoint = new MuleEndpoint("jcr:/"
				+ RepositoryTestSupport.getTestDataNode().getPath()
				+ "?contentPayloadType=full&eventTypes=" + Event.PROPERTY_ADDED
				+ "&deep=true&noLocal=false", true);

		JcrMessageReceiver messageReceiver = new JcrMessageReceiver(endpoint
				.getConnector(), (UMOComponent) mockComponent.proxy(), endpoint);

		JcrConnector connector = (JcrConnector) messageReceiver.getConnector();
		connector.setRepository(RepositoryTestSupport.getRepository());
		connector.initialise();
		connector.connect();

		messageReceiver.connect();
		messageReceiver.start();

		RepositoryTestSupport.getTestDataNode().setProperty("eventTrigger",
				"whatever");

		RepositoryTestSupport.getSession().save();

		Thread.sleep(100);

		messageReceiver.stop();
		messageReceiver.disconnect();
		messageReceiver.dispose();

		connector.disconnect();
		connector.dispose();

		mockComponent.verify();
		mockDescriptor.verify();
		mockInboundRouter.verify();
	}

}
