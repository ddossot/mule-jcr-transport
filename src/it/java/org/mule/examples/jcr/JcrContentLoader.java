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

import java.util.GregorianCalendar;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.springframework.beans.factory.annotation.Required;

/**
 * Load test content in a JCR repository.
 * 
 * @author David Dossot
 */
public class JcrContentLoader {
    private Repository repository;

    private String username;

    private String password;

    private String rootNodeName;

    public void initialize() throws LoginException, RepositoryException {
        final Session session = newSession();

        final Node repositoryRoot = session.getRootNode();

        if (repositoryRoot.hasNode(rootNodeName)) {
            repositoryRoot.getNode(rootNodeName).remove();
        }

        final Node rootNode = repositoryRoot.addNode(rootNodeName);

        // setup empty nodes as targets for single and multiple node creation
        rootNode.addNode("singleChild");
        rootNode.addNode("multipleChildren");

        // setup an empty target property for direct data writing
        rootNode.setProperty("targetProperty", "");

        // add some demo files
        final Node imagesNode = rootNode.addNode("images");
        storeImage(imagesNode, "mule.gif");
        storeImage(imagesNode, "jackrabbit.gif");

        session.save();
        session.logout();
    }

    private void storeImage(final Node imagesNode, final String imageName)
            throws RepositoryException {

        final Node imageNode = imagesNode.addNode(imageName, "nt:file");
        final Node contentNode =
                imageNode.addNode("jcr:content", "nt:resource");
        contentNode.setProperty(
                "jcr:data",
                Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "images/" + imageName));
        contentNode.setProperty("jcr:mimeType", "image/gif");
        contentNode.setProperty("jcr:lastModified",
                GregorianCalendar.getInstance());
    }

    private Session newSession() throws LoginException, RepositoryException {
        return repository.login(new SimpleCredentials(username,
                password.toCharArray()));
    }

    @Required
    public void setRepository(final Repository repository) {
        this.repository = repository;
    }

    @Required
    public void setPassword(final String password) {
        this.password = password;
    }

    @Required
    public void setUsername(final String username) {
        this.username = username;
    }

    @Required
    public void setRootNodeName(final String rootNodeName) {
        this.rootNodeName = rootNodeName;
    }

}
