package org.onebusaway.integration.api.where;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.onebusaway.integration.api.AbstractApiSupport;
import org.onebusaway.transit_data.model.TimeBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CurrentTimeTest extends AbstractApiSupport {

  @Test
  public void testXml() {
    Document document = getXml("/api/where/current-time.xml");
    Element data = verifyResponseWrapper(document, 1, 200);

    long time = getLong(data, "time");
    long delta = Math.abs(time - System.currentTimeMillis());
    assertTrue("comparing time delta: " + delta, delta < 1000);

    TimeBean timeBean = new TimeBean(new Date(time));
    assertEquals("compare readable time", timeBean.getReadableTime(), getText(
        data, "readableTime"));
  }
}
