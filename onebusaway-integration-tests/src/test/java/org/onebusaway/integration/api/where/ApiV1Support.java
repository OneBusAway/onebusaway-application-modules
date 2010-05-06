package org.onebusaway.integration.api.where;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.onebusaway.integration.api.AbstractApiSupport;
import org.w3c.dom.Element;

public abstract class ApiV1Support extends AbstractApiSupport {
  
  public static void verifyStop_1_13721(Element stop) {
    assertEquals("1_13721", getText(stop, "id"));
    assertEquals(47.668232, getDouble(stop, "lat"), 0.0);
    assertEquals(-122.376328, getDouble(stop, "lon"), 0.0);
    assertEquals("S", getText(stop, "direction"));
    assertEquals("15th Ave NW & NW Market St", getText(stop, "name"));
    assertEquals("13721", getText(stop, "code"));
    assertEquals("0", getText(stop, "locationType"));

    List<Element> routes = getElements(stop, "routes/route");
    assertEquals(1, routes.size());

    Element route = getElement(stop, "routes/route[id='1_15']");
    verifyRoute_1_15(route);
  }
  
  public static void verifyRoute_1_15 (Element route) {
    assertEquals("1_15", getText(route, "id"));
    assertEquals("15", getText(route, "shortName"));
    assertEquals("Blue Ridge/Downtown", getText(route, "description"));
    assertEquals("3", getText(route, "type"));
    assertEquals("http://metro.kingcounty.gov/tops/bus/schedules/s015_0_.html",
        getText(route, "url"));

    Element agency = getElement(route, "agency");
    verifyAgency_1(agency);
  }

  public static void verifyAgency_1(Element agency) {
    assertEquals("1", getText(agency, "id"));
    assertEquals("Metro Transit", getText(agency, "name"));
    assertEquals("http://metro.kingcounty.gov", getText(agency, "url"));
    assertEquals("America/Los_Angeles", getText(agency, "timezone"));
    assertEquals("en", getText(agency, "lang"));
    assertEquals("206-553-3000", getText(agency, "phone"));
  }
}
