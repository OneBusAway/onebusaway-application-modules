package org.onebusaway.integration.api.where;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.onebusaway.integration.api.AbstractApiSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StopsForLocationTest extends AbstractApiSupport {

  @Test
  public void testSpecificStop() {
    Document document = getXml("/api/where/stops-for-location.xml?lat=47.668&lon=-122.376&query=13721");
    Element data = verifyResponseWrapper(document, 1, 200);

    List<Element> stops = getElements(data, "stops/stop");
    assertEquals(1, stops.size());

    assertEquals("false", getText(data, "limitExceeded"));

    Element stop = stops.get(0);
    ApiV1Support.verifyStop_1_13721(stop);
  }

  @Test
  public void testStops() {
    Document document = getXml("/api/where/stops-for-location.xml?lat=47.668&lon=-122.376");
    Element data = verifyResponseWrapper(document, 1, 200);

    List<Element> stops = getElements(data, "stops/stop");
    assertEquals(12, stops.size());

    assertEquals("false", getText(data, "limitExceeded"));

    Element stop = getElement(data, "stops/stop[id='1_13721']");
    ApiV1Support.verifyStop_1_13721(stop);
  }
}
