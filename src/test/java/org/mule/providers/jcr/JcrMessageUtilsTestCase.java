package org.mule.providers.jcr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;

import javax.jcr.Session;
import javax.naming.CompositeName;

import junit.framework.TestCase;

import org.mule.util.IOUtils;

/**
 * @author David Dossot (david@dossot.net)
 */
public class JcrMessageUtilsTestCase extends TestCase {

	private Session session;

	private Object[] supportedValues = new Object[] { Boolean.TRUE,
			Calendar.getInstance(), new Double(3.14d),
			new ByteArrayInputStream("foo".getBytes()), "bar".getBytes(),
			new Long(123), "baz" };

	protected void setUp() throws Exception {
		super.setUp();
		session = RepositoryTestSupport.getSession();
	}

	public void testNewPropertyNullValue() throws Exception {
		try {
			JcrMessageUtils.newPropertyValue(session, null);
		} catch (IllegalArgumentException iae) {
			return;
		}
		fail("Should have got an IAE!");
	}

	public void testNewPropertyUnsupportedValue() throws Exception {
		try {
			JcrMessageUtils.newPropertyValue(session, new Object());
		} catch (IllegalArgumentException iae) {
			return;
		}
		fail("Should have got an IAE!");
	}

	public void testSimplePropertyValues() throws Exception {
		for (int i = 0; i < supportedValues.length; i++) {
			Object supportedValue = supportedValues[i];

			Object retrievedValue = JcrMessageUtils
					.getValuePayload(JcrMessageUtils.newPropertyValue(session,
							supportedValue));

			assertTrue(supportedValue + "!=" + retrievedValue, areEqual(
					supportedValue, retrievedValue));

		}
	}

	public void testSerializablePropertyValues() throws Exception {
		Serializable s = new CompositeName("a/b");

		InputStream retrievedValue = (InputStream) JcrMessageUtils
				.getValuePayload(JcrMessageUtils.newPropertyValue(session, s));

		ObjectInputStream ois = new ObjectInputStream(retrievedValue);
		Object deserializedValue = ois.readObject();
		ois.close();

		assertEquals(s, deserializedValue);
	}

	private boolean areEqual(final Object l, final Object r) throws Exception {
		if ((l instanceof InputStream) && (r instanceof InputStream)) {
			InputStream l1 = (InputStream) l;
			l1.reset();
			byte[] leftByteArray = IOUtils.toByteArray(l1);

			InputStream r1 = (InputStream) r;
			r1.reset();
			byte[] rightByteArray = IOUtils.toByteArray(r1);

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