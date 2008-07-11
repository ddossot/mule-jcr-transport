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

import org.mule.util.IOUtils;

/**
 * A simple demo client for the TCP image streamer service.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrImageStreamClient {

    public static void main(final String[] args) throws Exception {
        final String imageName = "mule.gif";

        Socket socket = new Socket("localhost", 9999);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(imageName.getBytes());
        outputStream.flush();
        socket.shutdownOutput();

        final InputStream inputStream = socket.getInputStream();
        final String imagePath =
                System.getProperty("java.io.tmpdir") + File.separatorChar
                        + imageName;
        IOUtils.copy(inputStream, new FileOutputStream(imagePath, false));
        inputStream.close();
        socket.close();
        System.out.println("Saved locally: " + imagePath);

        socket = new Socket("localhost", 9998);
        outputStream = socket.getOutputStream();
        outputStream.write("streamed content to store".getBytes());
        outputStream.flush();
        socket.shutdownOutput();
        socket.close();
        System.out.println("Data successfully streamed to Mule!");

        System.out.println("Done.");
    }
}
