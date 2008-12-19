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

/**
 * @author David Dossot (david@dossot.net)
 */
class TargetItem {
    private String absolutePath;

    private Item item;

    public TargetItem(final Item item, final String absolutePath) {
        this.item = item;
        this.absolutePath = absolutePath;
    }

    public Node asNodeOrNull() {
        return (item != null && item.isNode()) ? (Node) item : null;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public Item getItem() {
        return item;
    }

    public void setAbsolutePath(final String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public void setItem(final Item item) {
        this.item = item;
    }
}