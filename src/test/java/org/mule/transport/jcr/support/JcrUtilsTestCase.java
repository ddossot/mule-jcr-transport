package org.mule.transport.jcr.support;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;

import javax.jcr.Session;
import javax.naming.CompositeName;

import org.mule.api.MuleEvent;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.tck.MuleTestUtils;
import org.mule.transport.jcr.RepositoryTestSupport;
import org.mule.util.IOUtils;
import org.mule.util.StringUtils;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrUtilsTestCase extends AbstractMuleTestCase {

    private Session session;

    private final Object[] supportedValues =
            new Object[] { Boolean.TRUE, Calendar.getInstance(),
                    new Double(3.14d),
                    new ByteArrayInputStream("foo".getBytes()),
                    "bar".getBytes(), new Long(123), "baz" };

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        session = RepositoryTestSupport.getSession();
    }

    public void testParsePath() throws Exception {
        assertNull(JcrEventUtils.parseExpressionForEvent(null, null));

        assertEquals("foo", JcrEventUtils.parseExpressionForEvent("foo", null));

        assertEquals("#[foo]", JcrEventUtils.parseExpressionForEvent("#[foo]",
                null));

        final String path =
                "date:#[function:date];customDate:#[function:datestamp:yyyy];uuid:#[function:uuid];systime:#[function:now];eventProperty:#[header:eventProperty];missing:#[header:foo*]";

        assertNotNull(
                "The test hasn't been configured properly, no muleContext available",
                muleContext);
        final MuleEvent event =
                MuleTestUtils.getTestEvent("payload", muleContext);
        event.getMessage().setProperty("eventProperty", "bar");

        final String parsedPath =
                JcrEventUtils.parseExpressionForEvent(path, event);

        // all placeholders should have been resolved except foo
        assertEquals(1, StringUtils.countMatches(parsedPath, "#["));
        assertTrue(StringUtils.contains(parsedPath, "#[header:foo*]"));
    }

    public void testNewPropertyNullValue() throws Exception {
        try {
            JcrPropertyUtils.newPropertyValue(session, null);
        } catch (final IllegalArgumentException iae) {
            return;
        }
        fail("Should have got an IAE!");
    }

    public void testNewPropertyUnsupportedValue() throws Exception {
        try {
            JcrPropertyUtils.newPropertyValue(session, new Object());
        } catch (final IllegalArgumentException iae) {
            return;
        }
        fail("Should have got an IAE!");
    }

    public void testSimplePropertyValues() throws Exception {
        for (int i = 0; i < supportedValues.length; i++) {
            final Object supportedValue = supportedValues[i];

            final Object retrievedValue =
                    JcrPropertyUtils.getValuePayload(JcrPropertyUtils.newPropertyValue(
                            session, supportedValue));

            assertTrue(supportedValue + "!=" + retrievedValue, areEqual(
                    supportedValue, retrievedValue));

        }
    }

    public void testSerializablePropertyValues() throws Exception {
        final Serializable s = new CompositeName("a/b");

        final InputStream retrievedValue =
                (InputStream) JcrPropertyUtils.getValuePayload(JcrPropertyUtils.newPropertyValue(
                        session, s));

        final ObjectInputStream ois = new ObjectInputStream(retrievedValue);
        final Object deserializedValue = ois.readObject();
        ois.close();

        assertEquals(s, deserializedValue);
    }

    private boolean areEqual(final Object l, final Object r) throws Exception {
        if ((l instanceof InputStream) && (r instanceof InputStream)) {
            final InputStream l1 = (InputStream) l;
            l1.reset();
            final byte[] leftByteArray = IOUtils.toByteArray(l1);

            final InputStream r1 = (InputStream) r;
            r1.reset();
            final byte[] rightByteArray = IOUtils.toByteArray(r1);

            return Arrays.equals(leftByteArray, rightByteArray);

        } else if ((l instanceof InputStream) && (r instanceof byte[])) {
            return Arrays.equals(IOUtils.toByteArray((InputStream) l),
                    (byte[]) r);

        } else if ((l instanceof byte[]) && (r instanceof InputStream)) {
            return Arrays.equals((byte[]) l,
                    IOUtils.toByteArray((InputStream) r));

        } else if ((l instanceof byte[]) && (r instanceof byte[])) {
            return Arrays.equals((byte[]) l, (byte[]) r);

        } else {
            return l.equals(r);
        }
    }

}
