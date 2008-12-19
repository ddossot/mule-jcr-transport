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

/**
 * @author David Dossot (david@dossot.net)
 */
class QueryDefinition {
    private final String language;

    private final String statement;

    public QueryDefinition(final String language, final String statement) {
        this.language = language;
        this.statement = statement;
    }

    public String getLanguage() {
        return language;
    }

    public String getStatement() {
        return statement;
    }

    @Override
    public String toString() {
        return getLanguage() + ": " + getStatement();
    }

}