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

import javax.jcr.Item;
import javax.jcr.Session;

import org.mule.DefaultMuleMessage;
import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.routing.filter.Filter;
import org.mule.api.transformer.Transformer;
import org.mule.transport.AbstractMessageRequester;
import org.mule.transport.jcr.filters.JcrNodeNameFilter;
import org.mule.transport.jcr.filters.JcrPropertyNameFilter;

/**
 * <code>JcrMessageRequester</code> is responsible for receiving messages from
 * JCR repositories.
 * 
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageRequester extends AbstractMessageRequester {

	private final JcrConnector jcrConnector;

	private Session requesterSession;

	private final String nodeNamePatternFilter;

	private final String propertyNamePatternFilter;

	public Session getSession() {
		requesterSession = jcrConnector.validateSession(requesterSession);
		return requesterSession;
	}

	public JcrMessageRequester(final InboundEndpoint endpoint) {
		super(endpoint);

		jcrConnector = (JcrConnector) endpoint.getConnector();

		final Filter filter = getEndpoint().getFilter();

		nodeNamePatternFilter = JcrUtils.getPropertyNamePatternFilter(filter,
				JcrNodeNameFilter.class);

		propertyNamePatternFilter = JcrUtils.getPropertyNamePatternFilter(
				filter, JcrPropertyNameFilter.class);
	}

	@Override
	protected void doConnect() throws Exception {
		requesterSession = jcrConnector.newSession();
	}

	@Override
	protected void doDisconnect() throws Exception {
		jcrConnector.terminateSession(requesterSession);
	}

	@Override
	protected void doDispose() {
		requesterSession = null;
	}

	/**
	 * <p>
	 * Receives JCR content from the configured endpoint, using optional event
	 * properties to define the target repository item. Unless an exception is
	 * thrown, a <code>UMOMessage</code> will always be retrieved, possibly
	 * with a null payload if no content was acessible.
	 * </p>
	 * 
	 * <p>
	 * The content is extracted from the property or properties that were
	 * targeted by the endpoint path, filters and event optional parameters.
	 * </p>
	 * 
	 * <p>
	 * The first step of the content fetching consists in selecting a target
	 * item from the repository. By default, this item is a node selected by
	 * using the path of the endpoint. Alternatively, an event property (<code>JcrConnector.JCR_NODE_UUID_PROPERTY</code>)
	 * can be used to specify the UUID to use to select the target node: if this
	 * is done, the endpoint URI will be ignored. A third option is to define a
	 * query (with queryStatement and queryLanguage) that will be used to select
	 * the node. Then, if any optional relative paths have been specified as
	 * event properties for the current <code>UMOEvent</code> (<code>JcrConnector.JCR_NODE_RELPATH_PROPERTY</code>
	 * and/or <code>JcrConnector.JCR_PROPERTY_REL_PATH_PROPERTY</code>), they
	 * will be used to navigate to the actual item to use.
	 * </p>
	 * 
	 * <p>
	 * The second step consists in applying any <code>JcrNodeNameFilter</code>
	 * or <code>JcrPropertyNameFilter</code> that could have been defined on
	 * the endpoint to further narrow the target item from which content will be
	 * extracted. If more than one node is selected, the first one will be
	 * arbitrarily used as the target item and a warning will be issued. If no
	 * item can be selected, a null payload will be used for the returned
	 * <code>UMOMessage</code>.
	 * </p>
	 * 
	 * <p>
	 * The final step is the content extraction that will be used as the
	 * <code>UMOMessage</code> payload. For this, the following rules apply,
	 * depending on the target item:
	 * <ul>
	 * <li>For a single-valued property, the payload will be the property
	 * value.</li>
	 * <li>For a multi-valued property, the payload will be a <code>List</code>
	 * of values.</li>
	 * <li>For a node, the payload will be a <code>Map</code> of property
	 * names and property values (for these values, the previous two rules will
	 * apply).</li>
	 * </ul>
	 * </p>
	 * 
	 * @see org.mule.transport.jcr.JcrConnector Property names constants
	 * 
	 * @param ignoredTimeout
	 *            ignored timeout parameter.
	 * 
	 * @return the message fetched from this dispatcher.
	 */
	@Override
	protected MuleMessage doRequest(final long ignoredTimeout) throws Exception {
		final MuleEvent event = RequestContext.getEvent();

		if (logger.isDebugEnabled()) {
			if (event != null) {
				logger.debug("Receiving from JCR with event: " + event
						+ ", message: " + event.getMessage());
			} else {
				logger.debug("Receiving from JCR with no event.");
			}
		}

		final Item targetItem = JcrUtils.getTargetItem(getSession(),
				getEndpoint(), event, true);

		Object rawJcrContent = null;

		if (targetItem != null) {
			if (targetItem.isNode()) {
				rawJcrContent = JcrUtils.getRawContentFromNode(targetItem,
						nodeNamePatternFilter, propertyNamePatternFilter);
			} else {
				rawJcrContent = JcrUtils.getRawContentFromProperty(targetItem);
			}
		}

		final Object transformedContent = rawJcrContent == null ? null
				: ((Transformer) jcrConnector.getDefaultResponseTransformers()
						.get(0)).transform(rawJcrContent);

		return new DefaultMuleMessage(jcrConnector
				.getMessageAdapter(transformedContent));
	}

}
