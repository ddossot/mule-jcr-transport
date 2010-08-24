
package org.mule.examples.jcr;

import org.apache.commons.lang.RandomStringUtils;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.tck.functional.FunctionalTestComponent;

public class JcrTransportITCase extends FunctionalTestCase
{
    private MuleClient muleClient;

    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        muleClient = new MuleClient(muleContext);
    }

    @Override
    protected String getConfigResources()
    {
        return "jcr-example-config.xml";
    }

    public void testEventGenerationAndCapture() throws Exception
    {
        final String payload = RandomStringUtils.randomAlphanumeric(20);
        muleClient.dispatch("vm://mainIn", payload, null);

        final FunctionalTestComponent eventAccumulator = getFunctionalTestComponent("eventAccumulator");

        // wait for 3 messages to be received
        for (int i = 0; i < 20; i++)
        {
            Thread.sleep(500L);
            if (eventAccumulator.getReceivedMessagesCount() == 3) break;
        }

        assertEquals(3, eventAccumulator.getReceivedMessagesCount());

        // TODO check messages accumulated in FTC
        System.out.println("-------------------------------------");
        System.out.println(eventAccumulator.getReceivedMessage(1));
        // org.mule.transport.jcr.JcrMessage@b40443[path=/example/singleChild/targetSingleNtResourceNode/jcr:lastModified,type=4,typeAsString=PROPERTY_ADDED,userID=admin,content=java.util.GregorianCalendar[time=1282692377828,areFieldsSet=true,areAllFieldsSet=true,lenient=true,zone=sun.util.calendar.ZoneInfo[id="America/Vancouver",offset=-28800000,dstSavings=3600000,useDaylight=true,transitions=189,lastRule=java.util.SimpleTimeZone[id=America/Vancouver,offset=-28800000,dstSavings=3600000,useDaylight=true,startYear=0,startMode=3,startMonth=2,startDay=8,startDayOfWeek=1,startTime=7200000,startTimeMode=0,endMode=3,endMonth=10,endDay=1,endDayOfWeek=1,endTime=7200000,endTimeMode=0]],firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=1,YEAR=2010,MONTH=7,WEEK_OF_YEAR=35,WEEK_OF_MONTH=4,DAY_OF_MONTH=24,DAY_OF_YEAR=236,DAY_OF_WEEK=3,DAY_OF_WEEK_IN_MONTH=4,AM_PM=1,HOUR=4,HOUR_OF_DAY=16,MINUTE=26,SECOND=17,MILLISECOND=828,ZONE_OFFSET=-28800000,DST_OFFSET=3600000],uuid=<null>],
        // org.mule.transport.jcr.JcrMessage@14e1f2b[path=/example/singleChild/targetSingleNtResourceNode/jcr:data,type=4,typeAsString=PROPERTY_ADDED,userID=admin,content={98,69,55,109,54,80,107,76,72,98,109,108,67,65,72,51,103,53,106,100},uuid=<null>],
        // org.mule.transport.jcr.JcrMessage@c199f[path=/example/singleChild/targetSingleNtResourceNode/jcr:mimeType,type=4,typeAsString=PROPERTY_ADDED,userID=admin,content=text/plain,uuid=<null>],
        // org.mule.transport.jcr.JcrMessage@6646fc[path=/example/singleChild/targetSingleNtResourceNode/jcr:uuid,type=4,typeAsString=PROPERTY_ADDED,userID=admin,content=d456633e-5828-4092-b30e-51f8dade3859,uuid=<null>],
        // org.mule.transport.jcr.JcrMessage@be6858[path=/example/singleChild/targetSingleNtResourceNode/jcr:primaryType,type=4,typeAsString=PROPERTY_ADDED,userID=admin,content=nt:resource,uuid=<null>],
        // org.mule.transport.jcr.JcrMessage@b471fe[path=/example/singleChild/targetSingleNtResourceNode,type=1,typeAsString=NODE_ADDED,userID=admin,content=,uuid=d456633e-5828-4092-b30e-51f8dade3859]]

        System.out.println("-------------------------------------");
        System.out.println(eventAccumulator.getReceivedMessage(2));
        // org.mule.transport.jcr.JcrMessage@1c68b6f[path=/example/multipleChildren/targetMultipleUnstructuredNode/jcr:primaryType,type=4,typeAsString=PROPERTY_ADDED,userID=admin,content=nt:unstructured,uuid=<null>],
        // org.mule.transport.jcr.JcrMessage@c7f06[path=/example/multipleChildren/targetMultipleUnstructuredNode/jcr:data,type=4,typeAsString=PROPERTY_ADDED,userID=admin,content=bE7m6PkLHbmlCAH3g5jd,uuid=<null>],
        // org.mule.transport.jcr.JcrMessage@677ea2[path=/example/multipleChildren/targetMultipleUnstructuredNode,type=1,typeAsString=NODE_ADDED,userID=admin,content=,uuid=<null>]]

        System.out.println("-------------------------------------");
        System.out.println(eventAccumulator.getReceivedMessage(3));
        // org.mule.transport.jcr.JcrMessage@b8705b[path=/example/targetProperty,type=16,typeAsString=PROPERTY_CHANGED,userID=admin,content=bE7m6PkLHbmlCAH3g5jd,uuid=<null>]]
        System.out.println("-------------------------------------");
    }

    // TODO test jcrHttpContentFetcher
    // TODO test jcrStreamingTcpImageFetcher
    // TODO test jcrStreamingStoreToNewChildNode
    // TODO test jcrStreamingTcpFetcher
}
