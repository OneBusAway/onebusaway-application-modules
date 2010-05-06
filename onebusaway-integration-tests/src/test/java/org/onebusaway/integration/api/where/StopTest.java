package org.onebusaway.integration.api.where;

import org.junit.Test;
import org.onebusaway.integration.api.AbstractApiSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StopTest extends AbstractApiSupport {

  @Test
  public void testXml() {
    Document document = getXml("/api/where/stop/1_13721.xml");
    Element stop = verifyResponseWrapper(document, 1, 200);
    ApiV1Support.verifyStop_1_13721(stop);
  }
}
