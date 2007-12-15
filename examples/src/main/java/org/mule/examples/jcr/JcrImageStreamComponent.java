/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.examples.jcr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.mule.MuleManager;
import org.mule.impl.model.streaming.StreamingService;
import org.mule.providers.streaming.StreamMessageAdapter;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.util.IOUtils;

/**
 * Since the scripting component does not support streaming, this component has
 * been created. It performs the same functions as the scripted one visible in
 * the non streaming model of the example configuration.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrImageStreamComponent implements StreamingService {

	public void call(InputStream in, OutputStream out, UMOEventContext context)
			throws Exception {

		context.setStopFurtherProcessing(true);
		UMOMessage message = context.getMessage();
		message.clearProperties();
		message.setStringProperty("nodeRelPath", new String(IOUtils
				.toByteArray(in)));
		message.setStringProperty("Content-Type", "image/gif");

		UMOEndpoint ep = MuleManager.getInstance().lookupEndpoint("jcrImages");
		ep.setStreaming(true);

		IOUtils.copyLarge(((StreamMessageAdapter) ep.receive(0).getAdapter())
				.getInputStream(), out);

		out.close();
	}

	/**
	 * Demonstrates a pure TCP/IP client of the streamed JCR adapter.
	 */
	public static void main(String[] args) throws Exception {
		String imageName = "mule.gif";

		Socket socket = new Socket("localhost", 9999);
		OutputStream outputStream = socket.getOutputStream();
		outputStream.write(imageName.getBytes());
		outputStream.flush();
		socket.shutdownOutput();

		InputStream inputStream = socket.getInputStream();
		IOUtils.copy(inputStream, new FileOutputStream(System
				.getProperty("user.home")
				+ File.separatorChar + imageName));
		inputStream.close();
		socket.close();
	}
}
