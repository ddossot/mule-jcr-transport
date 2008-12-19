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

import java.io.Serializable;

/**
 * @author David Dossot (david@dossot.net)
 */
class EventContent {
    private Serializable data;

    private String uuid;

    public EventContent() {
        setData("");
        setUuid(null);
    }

    public Serializable getData() {
        return data;
    }

    public String getUuid() {
        return uuid;
    }

    public void setData(final Serializable data) {
        this.data = data;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}