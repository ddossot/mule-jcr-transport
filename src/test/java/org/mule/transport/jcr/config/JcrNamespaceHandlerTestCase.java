package org.mule.transport.jcr.config;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author David Dossot (ddossot@fivermedia.com)
 */
public class JcrNamespaceHandlerTestCase extends TestCase {
    public void testGetTokenizedValues() {
        assertNull("null", JcrNamespaceHandler.split(null));
        assertNull("blank", JcrNamespaceHandler.split("  \n "));

        assertEquals(
                Arrays.asList(new String[] { "foo", "bar" }),
                JcrNamespaceHandler.split("  \n foo  \t bar   \n "));

    }
}
