package org.onebusaway.federations.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.onebusaway.exceptions.InvalidArgumentServiceException;

import org.junit.Test;

public class AgencyIdSupportTest {
  @Test
  public void test() throws InvalidArgumentServiceException {

    assertEquals("a", AgencyIdSupport.getAgencyIdFromEntityId("a_b"));
    assertEquals("b", AgencyIdSupport.getAgencyIdFromEntityId("b_c_d"));

    try {
      AgencyIdSupport.getAgencyIdFromEntityId("ab");
      fail();
    } catch (InvalidArgumentServiceException ex) {

    }
  }
}
