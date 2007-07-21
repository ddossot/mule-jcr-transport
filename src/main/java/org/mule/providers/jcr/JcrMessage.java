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

import java.io.Serializable;

import javax.jcr.observation.Event;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Provides an immutable JCR message, which is an augmented and serializable
 * <code>javax.jcr.observation.Event</code>.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrMessage implements Event, Serializable {
	static final String UNKNOWN_EVENT_TYPE = "UNKNOWN";

	private static final long serialVersionUID = -7200906980423201081L;

	private final String path;

	private final int type;

	private final String typeAsString;

	private final String userID;

	private final Serializable content;

	public JcrMessage(final String path, final int type,
			final String typeAsString, final String userID,
			final Serializable content) {

		this.path = path;
		this.type = type;
		this.typeAsString = typeAsString;
		this.userID = userID;
		this.content = content;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/**
	 * @return the content
	 */
	public Serializable getContent() {
		return content;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the typeAsString
	 */
	public String getTypeAsString() {
		return typeAsString;
	}

	/**
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

}
