package org.mule.providers.jcr;

import junit.framework.TestCase;

/**
 * TODO comment
 *
 * @author David Dossot (david@dossot.net)
 */
public class JcrContentPayloadTypeTestCase extends TestCase {

    // FIXME test equals / hashcode

    public void testFromStringLowerCaseEquals() {
        assertEquals(JcrContentPayloadType.NONE,
                JcrContentPayloadType.fromString("none"));

        assertEquals(JcrContentPayloadType.NO_BINARY,
                JcrContentPayloadType.fromString("nobinary"));

        assertEquals(JcrContentPayloadType.FULL,
                JcrContentPayloadType.fromString("full"));
    }

    public void testFromStringAnyCaseEquals() {
        assertEquals(JcrContentPayloadType.NONE,
                JcrContentPayloadType.fromString("nONe"));

        assertEquals(JcrContentPayloadType.NO_BINARY,
                JcrContentPayloadType.fromString("NoBinary"));

        assertEquals(JcrContentPayloadType.FULL,
                JcrContentPayloadType.fromString("FULL"));
    }

    public void testFromStringLowerCaseSame() {
        assertSame(JcrContentPayloadType.NONE,
                JcrContentPayloadType.fromString("none"));

        assertSame(JcrContentPayloadType.NO_BINARY,
                JcrContentPayloadType.fromString("nobinary"));

        assertSame(JcrContentPayloadType.FULL,
                JcrContentPayloadType.fromString("full"));
    }

    public void testFromStringAnyCaseSame() {
        assertSame(JcrContentPayloadType.NONE,
                JcrContentPayloadType.fromString("nONe"));

        assertSame(JcrContentPayloadType.NO_BINARY,
                JcrContentPayloadType.fromString("NoBinary"));

        assertSame(JcrContentPayloadType.FULL,
                JcrContentPayloadType.fromString("FULL"));
    }

}
