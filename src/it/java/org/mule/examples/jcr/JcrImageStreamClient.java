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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.mule.util.IOUtils;

/**
 * A simple demo client for the TCP image streamer service.
 * 
 * @author David Dossot (david@dossot.net)
 */
public final class JcrImageStreamClient
{
    public static void main(final String[] args) throws Exception
    {
        downloadRelativePath();
        downloadAbsolutePath();
        uploadStreamingData();

        System.out.println("Done.");
    }

    private static void downloadRelativePath()
        throws UnknownHostException, IOException, FileNotFoundException
    {
        downloadContentToFile("mule.gif", 9999, "mule.gif");
    }

    private static void downloadAbsolutePath()
        throws UnknownHostException, IOException, FileNotFoundException
    {
        downloadContentToFile("/example/images/jackrabbit.gif/jcr:content/jcr:data", 9997, "jackrabbit.gif");
    }

    private static void downloadContentToFile(final String contentReference,
                                              final int port,
                                              final String localName)
        throws UnknownHostException, IOException, FileNotFoundException
    {
        final String contentPath = System.getProperty("java.io.tmpdir") + File.separatorChar + localName;
        downloadContentToStream(contentReference, port, new FileOutputStream(contentPath, false));
        System.out.println("Saved locally: " + contentPath);
    }

    public static void downloadContentToStream(final String contentReference,
                                               final int port,
                                               final OutputStream stream)
        throws UnknownHostException, IOException, FileNotFoundException
    {

        final Socket socket = new Socket("localhost", port);
        final OutputStream outputStream = socket.getOutputStream();
        outputStream.write(contentReference.getBytes());
        outputStream.flush();
        socket.shutdownOutput();

        final InputStream inputStream = socket.getInputStream();
        IOUtils.copy(inputStream, stream);
        inputStream.close();
        socket.close();
    }

    public static void uploadStreamingData() throws UnknownHostException, IOException
    {
        Socket socket;
        OutputStream outputStream;
        socket = new Socket("localhost", 9998);
        outputStream = socket.getOutputStream();
        outputStream.write("streamed content to store".getBytes());
        outputStream.flush();
        socket.shutdownOutput();
        socket.close();
        System.out.println("Data successfully streamed to Mule!");
    }
}
