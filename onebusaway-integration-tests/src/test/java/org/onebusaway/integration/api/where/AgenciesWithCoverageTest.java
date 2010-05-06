package org.onebusaway.integration.api.where;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.onebusaway.integration.api.AbstractApiSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AgenciesWithCoverageTest extends AbstractApiSupport {

  @Test
  public void testXml() {
    Document document = getXml("/api/where/agencies-with-coverage.xml");
    Element data = verifyResponseWrapper(document, 1, 200);

    List<Element> elements = getElements(data,
        "agency-with-coverage[agency/id='1']");
    assertEquals(1, elements.size());

    Element agencyWithCoverage = elements.get(0);
    assertEquals(47.5607395, getDouble(agencyWithCoverage, "lat"), 0.1);
    assertEquals(-122.29384168794557, getDouble(agencyWithCoverage, "lon"), 0.2);
    Element agency = getElement(agencyWithCoverage, "agency");
    ApiV1Support.verifyAgency_1(agency);
  }
}
