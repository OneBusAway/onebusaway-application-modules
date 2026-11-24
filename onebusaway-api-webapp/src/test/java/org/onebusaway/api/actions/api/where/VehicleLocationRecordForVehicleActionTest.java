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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onebusaway.transit_data.services.TransitDataService;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.XWorkTestCaseHelper;

@RunWith(MockitoJUnitRunner.class)
public class VehicleLocationRecordForVehicleActionTest {

  @Mock
  private TransitDataService _service;

  @InjectMocks
  private VehicleLocationRecordForVehicleAction _action;

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
    ValueStack stack = ActionContext.getContext().getValueStack();
    stack.push(_action);
    stack.setParameter("id", new String[] {"1_1234"});
    stack.setParameter("time", new String[] {"1792861260000"});

    _action.show();

    verify(_service).getVehicleLocationRecordForVehicleId(eq("1_1234"), eq(1792861260000L));
  }
}
