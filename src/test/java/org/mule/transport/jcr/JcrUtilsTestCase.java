package org.mule.transport.jcr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;

import javax.jcr.Session;
import javax.naming.CompositeName;

import junit.framework.TestCase;

import org.mule.api.MuleEvent;
import org.mule.tck.MuleTestUtils;
import org.mule.util.IOUtils;
import org.mule.util.StringUtils;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrUtilsTestCase extends TestCase {

	private Session session;

	private final Object[] supportedValues = new Object[] { Boolean.TRUE,
			Calendar.getInstance(), new Double(3.14d),
			new ByteArrayInputStream("foo".getBytes()), "bar".getBytes(),
			new Long(123), "baz" };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		session = RepositoryTestSupport.getSession();
	}

	public void testParsePath() throws Exception {
		assertNull(JcrUtils.parsePath(null, null));

		assertEquals("foo", JcrUtils.parsePath("foo", null));

		assertEquals("${foo}", JcrUtils.parsePath("${foo}", null));

		final String path = "date:${DATE};customDate:${DATE:yyyy};uuid:${UUID};systime:${SYSTIME};eventProperty:${eventProperty};missing:${foo}";

		final MuleEvent event = MuleTestUtils.getTestEvent("payload");
		event.getMessage().setProperty("eventProperty", "bar");

		final String parsedPath = JcrUtils.parsePath(path, event);

		// all placeholders should have been resolved except foo
		assertEquals(1, StringUtils.countMatches(parsedPath, "${"));
		assertTrue(StringUtils.contains(parsedPath, "${foo}"));
	}

	public void testNewPropertyNullValue() throws Exception {
		try {
			JcrUtils.newPropertyValue(session, null);
		} catch (final IllegalArgumentException iae) {
			return;
		}
		fail("Should have got an IAE!");
	}

	public void testNewPropertyUnsupportedValue() throws Exception {
		try {
			JcrUtils.newPropertyValue(session, new Object());
		} catch (final IllegalArgumentException iae) {
			return;
		}
		fail("Should have got an IAE!");
	}

	public void testSimplePropertyValues() throws Exception {
		for (int i = 0; i < supportedValues.length; i++) {
			final Object supportedValue = supportedValues[i];

			final Object retrievedValue = JcrUtils.getValuePayload(JcrUtils
					.newPropertyValue(session, supportedValue));

			assertTrue(supportedValue + "!=" + retrievedValue, areEqual(
					supportedValue, retrievedValue));

		}
	}

	public void testSerializablePropertyValues() throws Exception {
		final Serializable s = new CompositeName("a/b");

		final InputStream retrievedValue = (InputStream) JcrUtils
				.getValuePayload(JcrUtils.newPropertyValue(session, s));

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
			return Arrays.equals((byte[]) l, IOUtils
					.toByteArray((InputStream) r));

		} else if ((l instanceof byte[]) && (r instanceof byte[])) {
			return Arrays.equals((byte[]) l, (byte[]) r);

		} else {
			return l.equals(r);
		}
	}

}
