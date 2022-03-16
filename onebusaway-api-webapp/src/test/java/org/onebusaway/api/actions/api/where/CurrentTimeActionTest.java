/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.onebusaway.api.model.transit.EntryWithReferencesBean;
import org.onebusaway.utility.DateLibrary;

public class CurrentTimeActionTest {

  @Test
  public void test() throws ParseException {

    CurrentTimeAction action = new CurrentTimeAction();

    long t = System.currentTimeMillis();

    DefaultHttpHeaders headers = action.index();
    assertEquals(200, headers.getStatus());

    ResponseBean response = action.getModel();
    assertEquals(200, response.getCode());
    assertEquals(2, response.getVersion());

    @SuppressWarnings("unchecked")
    EntryWithReferencesBean<TimeBean> entry = (EntryWithReferencesBean<TimeBean>) response.getData();
    TimeBean time = entry.getEntry();
    
    assertNotNull(time);

    long delta = Math.abs(time.getTime() - t);
    assertTrue("check that time delta is within limits: delta=" + delta,
        delta < 500);

    String readableTime = DateLibrary.getTimeAsIso8601String(new Date(
        time.getTime()));
    assertEquals(readableTime, time.getReadableTime());
  }
}
