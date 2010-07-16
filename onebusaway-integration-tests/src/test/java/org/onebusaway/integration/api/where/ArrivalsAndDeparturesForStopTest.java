package org.onebusaway.integration.api.where;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.onebusaway.integration.TestSupport;
import org.onebusaway.integration.api.AbstractApiSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ArrivalsAndDeparturesForStopTest extends AbstractApiSupport {

  @Test
  public void testXml() {
    Document document = getXml("/api/where/arrivals-and-departures-for-stop/1_13721.xml");
    Element data = verifyResponseWrapper(document, 1, 200);

    Element stop = getElement(data, "stop");
    ApiV1Support.verifyStop_1_13721(stop);

    List<Element> arrivalsAndDepartures = getElements(data,
        "arrivalsAndDepartures/arrivalAndDeparture");

    if (arrivalsAndDepartures.isEmpty()) {
      if( TestSupport.checkArrivalsForRoute15())
        fail("expected arrivals in this time range");
    }

    long t = System.currentTimeMillis();
    long delta = 40 * 60 * 1000;

    for (Element el : arrivalsAndDepartures) {
      assertTrue(getText(el, "routeId").matches("^\\d+_\\d+$"));
      assertTrue(getText(el, "routeShortName").matches("^\\d+E{0,1}$"));
      assertTrue(getText(el, "tripId").matches("^\\d+_\\d+$"));
      assertTrue(getText(el, "tripHeadsign").matches("^[-\\w\\s]+$"));
      assertEquals("1_13721", getText(el, "stopId"));

      assertTrue(Math.abs(getLong(el, "scheduledArrivalTime") - t) < delta);
      assertTrue(Math.abs(getLong(el, "scheduledDepartureTime") - t) < delta);

      long pat = getLong(el, "predictedArrivalTime");
      assertTrue(pat == 0 || Math.abs(pat - t) < delta);

      long pdt = getLong(el, "predictedDepartureTime");
      assertTrue(pdt == 0 || Math.abs(pdt - t) < delta);
    }

    List<Element> nearbyStops = getElements(data, "nearbyStops/stop");
    assertEquals(1, nearbyStops.size());

    Element nearbyStop = nearbyStops.get(0);
    assertEquals("1_14230", getText(nearbyStop, "id"));
  }
}
