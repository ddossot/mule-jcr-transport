
package org.mule.transport.jcr.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrNamespaceHandlerTestCase
{
    @Test
    public void testGetTokenizedValues()
    {
        assertNull("null", JcrNamespaceHandler.split(null));
        assertNull("blank", JcrNamespaceHandler.split("  \n "));

        assertEquals(Arrays.asList(new String[]{"foo", "bar"}),
            JcrNamespaceHandler.split("  \n foo  \t bar   \n "));
    }
}
