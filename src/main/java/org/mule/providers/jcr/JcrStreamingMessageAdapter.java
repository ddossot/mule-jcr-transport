/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.providers.jcr;

import java.io.InputStream;
import java.io.OutputStream;

import org.mule.providers.streaming.StreamMessageAdapter;
import org.mule.umo.provider.OutputHandler;

/**
 * <code>Jcr</code> TODO document
 */
public class JcrStreamingMessageAdapter extends StreamMessageAdapter {

	private static final long serialVersionUID = -8425104094679642435L;

	public JcrStreamingMessageAdapter(InputStream in) {
		super(in);
	}

	public JcrStreamingMessageAdapter(InputStream in, OutputStream out) {
		super(in, out);
	}

	public JcrStreamingMessageAdapter(OutputHandler handler) {
		super(handler);
	}

	public JcrStreamingMessageAdapter(OutputStream out, OutputHandler handler) {
		super(out, handler);
	}

	public JcrStreamingMessageAdapter(InputStream in, OutputStream out,
			OutputHandler handler) {
		super(in, out, handler);
	}

	public void release() {
		// Optional; does not need to be implemented. Delete if not required

		/*
		 * IMPLEMENTATION NOTE: The release method is called by Mule to notify
		 * this adapter that it is no longer needed. This method can be used to
		 * release any resources that a custom StreamAdapter may have associated
		 * with it.
		 */
	}
}
