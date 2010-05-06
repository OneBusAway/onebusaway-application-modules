package org.onebusaway.integration.api.where;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.onebusaway.integration.api.AbstractApiSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RoutesForLocationTest extends AbstractApiSupport {

  @Test
  public void testSpecificRoute() {
    Document document = getXml("/api/where/routes-for-location.xml?lat=47.668&lon=-122.376&query=15");
    Element data = verifyResponseWrapper(document, 1, 200);

    List<Element> routes = getElements(data, "routes/route");
    assertEquals(1, routes.size());

    assertEquals("false", getText(data, "limitExceeded"));

    Element route = routes.get(0);
    ApiV1Support.verifyRoute_1_15(route);
  }

  @Test
  public void testRoutes() {
    Document document = getXml("/api/where/routes-for-location.xml?lat=47.668&lon=-122.376&radius=100");
    Element data = verifyResponseWrapper(document, 1, 200);

    List<Element> routes = getElements(data, "routes/route");
    assertEquals(1, routes.size());
    
    assertEquals("false",getText(data,"limitExceeded"));
    
    Element route = getElement(data,"routes/route[id='1_15']");
    ApiV1Support.verifyRoute_1_15(route);
  }
}
