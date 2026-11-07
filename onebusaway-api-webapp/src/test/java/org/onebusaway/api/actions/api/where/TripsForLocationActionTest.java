/**
 * Copyright (C) 2026 Open Transit Software Foundation
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.XWorkTestCaseHelper;

@RunWith(MockitoJUnitRunner.class)
public class TripsForLocationActionTest {

  @Mock
  private TransitDataService _service;

  @InjectMocks
  private TripsForLocationAction _action;

  @Before
  public void before() throws Exception {
    XWorkTestCaseHelper.setUp();
  }

  @After
  public void after() throws Exception {
    XWorkTestCaseHelper.tearDown(null);
  }

  /**
   * The {@code time} request parameter is applied through the Struts value
   * stack, the same way {@code ParametersInterceptor} does it for a real
   * request. {@code DateTimeConverter} produces a {@link java.util.Date}, so
   * a setter declared with {@link java.sql.Date} is never called and the
   * action silently falls back to the current time (issue #486).
   */
  @Test
  public void testTimeParameter() throws Exception {
    when(_service.getTripsForBounds(any(TripsForBoundsQueryBean.class))).thenReturn(
        new ListBean<TripDetailsBean>(new ArrayList<TripDetailsBean>(), false));

    ValueStack stack = ActionContext.getContext().getValueStack();
    stack.push(_action);
    stack.setParameter("lat", new String[] {"47.6004"});
    stack.setParameter("lon", new String[] {"-122.33"});
    stack.setParameter("time", new String[] {"1792861260000"});

    _action.index();

    ArgumentCaptor<TripsForBoundsQueryBean> captor = ArgumentCaptor.forClass(TripsForBoundsQueryBean.class);
    verify(_service).getTripsForBounds(captor.capture());
    assertEquals(1792861260000L, captor.getValue().getTime());
  }
}
