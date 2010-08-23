/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr.support;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.transport.jcr.JcrContentPayloadType;

/**
 * @author David Dossot (david@dossot.net)
 */
public abstract class JcrEventUtils
{

    private static final Log LOG = LogFactory.getLog(JcrEventUtils.class);

    // This should really be in JCR API!
    public static String getEventTypeNameFromValue(final int eventType)
    {
        switch (eventType)
        {

            case Event.NODE_ADDED :
                return "NODE_ADDED";

            case Event.NODE_REMOVED :
                return "NODE_REMOVED";

            case Event.PROPERTY_ADDED :
                return "PROPERTY_ADDED";

            case Event.PROPERTY_CHANGED :
                return "PROPERTY_CHANGED";

            case Event.PROPERTY_REMOVED :
                return "PROPERTY_REMOVED";

            default :
                return "UNKNOWN";
        }
    }

    public static String getParsableEventProperty(final MuleEvent event, final String propertyName)
    {
        final String expression = (String) event.getMessage().findPropertyInAnyScope(propertyName, null);

        return parseExpressionForEvent(expression, event);
    }

    static String parseExpressionForEvent(final String expression, final MuleEvent event)
    {
        if (event == null)
        {
            return expression;
        }

        if (expression == null)
        {
            return null;
        }

        return event.getMuleContext().getExpressionManager().parse(expression, event.getMessage(), false);
    }

    static EventContent getEventContent(final Event event,
                                        final Session session,
                                        final JcrContentPayloadType contentPayloadType)
    {

        final EventContent result = new EventContent();

        if (!JcrContentPayloadType.NONE.equals(contentPayloadType))
        {

            final int eventType = event.getType();

            // tentatively add content from the path of the event if the
            // event is not a removal if the content can not be fetched (because
            // it has changed between the moment the event was raised and the
            // moment we build this message), report the error at info level
            // only (this is a failure that can happen and is not business
            // critical in any way).
            String eventPath = "N/A";

            try
            {
                if ((eventType == Event.PROPERTY_ADDED) || (eventType == Event.PROPERTY_CHANGED))
                {

                    eventPath = event.getPath();
                    final Item item = session.getItem(eventPath);

                    if (!item.isNode())
                    {
                        // is not a node == is a property
                        result.setData(JcrPropertyUtils.outputProperty(eventPath, (Property) item,
                            contentPayloadType));
                    }

                }
                else if (eventType == Event.NODE_ADDED)
                {
                    eventPath = event.getPath();
                    final Item item = session.getItem(eventPath);

                    if (item.isNode())
                    {
                        final Node node = ((Node) item);
                        if (node.isNodeType("mix:referenceable"))
                        {
                            result.setUuid(node.getUUID());
                        }
                    }
                }
            }
            catch (final RepositoryException ignoredException)
            {
                if (LOG.isInfoEnabled())
                {
                    LOG.info("Can not fetch content for event path: " + eventPath + "("
                             + ignoredException.getMessage() + ")");
                }
            }

        }

        return result;
    }

    private JcrEventUtils()
    {
        throw new UnsupportedOperationException("Do not instantiate");
    }

}
