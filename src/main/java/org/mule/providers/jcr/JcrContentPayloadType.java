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

/**
 * A typesafe enum that represents the supported content paylod types for a JCR
 * event.
 * 
 * @author David Dossot (david@dossot.net)
 */
final class JcrContentPayloadType {

	/**
	 * No content will be fetched from JCR: the payload will be limited to the
	 * event information.
	 */
	public static final JcrContentPayloadType NONE = new JcrContentPayloadType(
			"none");

	/**
	 * The payload will contain event information and data from the node source
	 * of the event, only if this data is not of bynary type.
	 */
	public static final JcrContentPayloadType NO_BINARY = new JcrContentPayloadType(
			"nobinary");

	/**
	 * The payload will contain event information and data from the node source
	 * of the event.
	 */
	public static final JcrContentPayloadType FULL = new JcrContentPayloadType(
			"full");

	private static final JcrContentPayloadType[] ALL_TYPES = new JcrContentPayloadType[] {
			NONE, NO_BINARY, FULL };

	private final String contentPayloadType;

	private JcrContentPayloadType(final String contentPayloadType) {
		this.contentPayloadType = contentPayloadType;
	}

	public String toString() {
		return contentPayloadType;
	}

	public int hashCode() {
		return contentPayloadType.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj instanceof JcrContentPayloadType) {
			return contentPayloadType
					.equals(((JcrContentPayloadType) obj).contentPayloadType);
		} else {
			return false;
		}
	}

	public static JcrContentPayloadType fromString(String type)
			throws IllegalArgumentException {

		if ((type == null) || (type.trim().equals(""))) {
			return NONE;
		}

		if (!type.equals("")) {
			for (int i = 0; i < ALL_TYPES.length; i++) {
				JcrContentPayloadType contentPayload = ALL_TYPES[i];

				if (contentPayload.contentPayloadType.equalsIgnoreCase(type)) {
					return contentPayload;
				}
			}
		}

		throw new IllegalArgumentException("Invalid content payload type: "
				+ type);
	}

}
