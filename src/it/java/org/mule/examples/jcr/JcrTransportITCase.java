
package org.mule.examples.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.mule.module.client.MuleClient;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.jcr.JcrMessage;

public class JcrTransportITCase extends FunctionalTestCase
{
    private MuleClient muleClient;
    private FunctionalTestComponent eventAccumulator;

    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        muleClient = new MuleClient(muleContext);
        eventAccumulator = getFunctionalTestComponent("eventAccumulator");
        eventAccumulator.initialise();
    }

    @Override
    protected String getConfigResources()
    {
        return "jcr-example-config.xml";
    }

    @Test
    public void testEventGenerationAndCapture() throws Exception
    {
        final String payload = RandomStringUtils.randomAlphanumeric(20);
        muleClient.dispatch("vm://mainIn", payload, null);

        final int receivedMessagesCount = waitForNMessages(3);

        final Map<String, Integer> expectedEventsCountMap = new HashMap<String, Integer>();
        expectedEventsCountMap.put("/example/singleChild/targetSingleNtResourceNode", 6);
        expectedEventsCountMap.put("/example/multipleChildren/targetMultipleUnstructuredNode", 3);
        expectedEventsCountMap.put("/example/targetProperty", 1);

        for (int i = 1; i <= receivedMessagesCount; i++)
        {
            final List<JcrMessage> events = getAccumulatedJcrMessageList(i);

            int expectedEventsCount = 0;
            String rootEventPath = null;
            boolean contentFound = false;

            for (final JcrMessage event : events)
            {
                final String eventPath = event.getPath();

                if (expectedEventsCountMap.containsKey(eventPath))
                {
                    expectedEventsCount = expectedEventsCountMap.get(eventPath);
                    rootEventPath = eventPath;
                }

                final Serializable eventContent = event.getContent();

                if ((eventContent instanceof String && payload.equals(eventContent))
                    || (eventContent.getClass().isArray() && payload.equals(new String((byte[]) eventContent))))
                {
                    contentFound = true;
                }
            }

            assertEquals(expectedEventsCount, events.size());
            assertTrue("content not found for: " + rootEventPath, contentFound);
        }
    }

    @Test
    public void testRequesterWithRelPath() throws Exception
    {
        final byte[] muleImage = muleClient.request("http://localhost:8080/images/mule.gif", 5000)
            .getPayloadAsBytes();

        assertEquals(1552, muleImage.length);
    }

    @Test
    public void testRequesterWithNodePath() throws Exception
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JcrImageStreamClient.downloadContentToStream("/example/images/jackrabbit.gif/jcr:content/jcr:data",
            9997, baos);

        assertEquals(2440, baos.size());
    }

    @Test
    public void testOutboundStreaming() throws Exception
    {
        JcrImageStreamClient.uploadStreamingData();
        waitForNMessages(1);
        final List<JcrMessage> accumulatedJcrMessageList = getAccumulatedJcrMessageList(1);
        assertEquals(6, accumulatedJcrMessageList.size());
    }

    private int waitForNMessages(final int expectedMessagesCount) throws InterruptedException
    {
        int receivedMessagesCount = 0;
        for (int i = 0; i < 20; i++)
        {
            System.out.printf("Received %d message(s) when %d are expected: pondering...\n",
                receivedMessagesCount, expectedMessagesCount);

            Thread.sleep(500L);

            if ((receivedMessagesCount = eventAccumulator.getReceivedMessagesCount()) == expectedMessagesCount)
            {
                break;
            }
        }

        assertEquals(expectedMessagesCount, receivedMessagesCount);
        return receivedMessagesCount;
    }

    private List<JcrMessage> getAccumulatedJcrMessageList(final int i)
    {
        final Object receivedMessage = eventAccumulator.getReceivedMessage(i);
        assertTrue(receivedMessage instanceof List<?>);

        @SuppressWarnings("unchecked")
        final List<JcrMessage> events = (List<JcrMessage>) receivedMessage;
        return events;
    }
}
