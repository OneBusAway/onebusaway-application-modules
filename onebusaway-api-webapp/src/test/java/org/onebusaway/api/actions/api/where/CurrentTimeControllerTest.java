package org.onebusaway.api.actions.api.where;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.junit.Test;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.TimeBean;
import org.onebusaway.utility.DateLibrary;

public class CurrentTimeControllerTest {

  @Test
  public void test() throws ParseException {

    CurrentTimeController controller = new CurrentTimeController();

    long t = System.currentTimeMillis();

    DefaultHttpHeaders headers = controller.index();
    assertEquals(200, headers.getStatus());

    ResponseBean response = controller.getModel();
    assertEquals(200, response.getCode());
    assertEquals(1, response.getVersion());

    TimeBean time = (TimeBean) response.getData();

    assertNotNull(time);

    long delta = Math.abs(time.getTime() - t);
    assertTrue("check that time delta is within limits: delta=" + delta,
        delta < 100);

    String readableTime = DateLibrary.getTimeAsIso8601String(new Date(
        time.getTime()));
    assertEquals(readableTime, time.getReadableTime());
  }
}
