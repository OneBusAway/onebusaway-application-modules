package org.onebusaway.integration.api.where;

import org.junit.Test;
import org.onebusaway.integration.api.AbstractApiSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RouteTest extends AbstractApiSupport {

  @Test
  public void testXml() {
    Document document = getXml("/api/where/route/1_15.xml");
    Element route = verifyResponseWrapper(document, 1, 200);
    ApiV1Support.verifyRoute_1_15(route);
  }
}
