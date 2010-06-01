/**
 * 
 */
package org.mule.transport.jcr;

import java.io.ByteArrayInputStream;

import javax.jcr.Node;

import org.mule.RequestContext;
import org.mule.tck.AbstractMuleTestCase;

/**
 * @author David Dossot (david@dossot.net)
 * 
 */
public abstract class AbstractJcrMessagerTestCase extends AbstractMuleTestCase {

    protected JcrConnector connector;

    protected String uuid;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();

        // create some extra test nodes and properties
        RepositoryTestSupport.resetRepository();

        final Node testDataNode = RepositoryTestSupport.getTestDataNode();
        testDataNode.setProperty("pi", Math.PI);
        testDataNode.setProperty("stream", new ByteArrayInputStream("test".getBytes()));
        testDataNode.setProperty("text", "EHLO SPAM");
        testDataNode.addMixin("mix:referenceable");
        uuid = testDataNode.getUUID();

        final Node target = testDataNode.addNode("noderelpath-target");
        target.setProperty("proprelpath-target", 123L);

        RepositoryTestSupport.getSession().save();
    }

    @Override
    protected void doTearDown() throws Exception {
        RequestContext.setEvent(null);

        connector.stop();
        connector.disconnect();
        connector.dispose();
        super.doTearDown();
    }

}