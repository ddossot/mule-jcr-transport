/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the MuleSource MPL
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrContentPayloadTypeTestCase
{

    @Test
    public void testFromStringLowerCaseEquals()
    {
        assertEquals(JcrContentPayloadType.NONE, JcrContentPayloadType.fromString("none"));

        assertEquals(JcrContentPayloadType.NO_BINARY, JcrContentPayloadType.fromString("nobinary"));

        assertEquals(JcrContentPayloadType.FULL, JcrContentPayloadType.fromString("full"));
    }

    @Test
    public void testFromStringAnyCaseEquals()
    {
        assertEquals(JcrContentPayloadType.NONE, JcrContentPayloadType.fromString("nONe"));

        assertEquals(JcrContentPayloadType.NO_BINARY, JcrContentPayloadType.fromString("NoBinary"));

        assertEquals(JcrContentPayloadType.FULL, JcrContentPayloadType.fromString("FULL"));
    }

    @Test
    public void testFromStringLowerCaseSame()
    {
        assertSame(JcrContentPayloadType.NONE, JcrContentPayloadType.fromString("none"));

        assertSame(JcrContentPayloadType.NO_BINARY, JcrContentPayloadType.fromString("nobinary"));

        assertSame(JcrContentPayloadType.FULL, JcrContentPayloadType.fromString("full"));
    }

    @Test
    public void testFromStringAnyCaseSame()
    {
        assertSame(JcrContentPayloadType.NONE, JcrContentPayloadType.fromString("nONe"));

        assertSame(JcrContentPayloadType.NO_BINARY, JcrContentPayloadType.fromString("NoBinary"));

        assertSame(JcrContentPayloadType.FULL, JcrContentPayloadType.fromString("FULL"));
    }

    @Test
    public void testEqualsMarginalCases()
    {
        assertFalse(JcrContentPayloadType.NONE.equals(null));
        assertFalse(JcrContentPayloadType.NONE.equals(new Object()));
    }

    @Test
    public void testFromStringMarginalCases()
    {
        assertEquals(JcrContentPayloadType.NONE, JcrContentPayloadType.fromString(null));

        assertEquals(JcrContentPayloadType.NONE, JcrContentPayloadType.fromString(""));

        assertEquals(JcrContentPayloadType.NONE, JcrContentPayloadType.fromString(" "));

        try
        {
            JcrContentPayloadType.fromString("foo-bar");

            fail("Should have thrown an IllegalArgumentException");
        }
        catch (final IllegalArgumentException iea)
        {
            // expected
        }
    }

}
