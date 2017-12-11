/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions;

import static org.junit.Assert.assertEquals;

import org.onebusaway.utility.DateLibrary;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class IndexActionTest {

  @Test
  public void testExecute() throws Exception {

    ActionProxy proxy = Mockito.mock(ActionProxy.class);
    Mockito.when(proxy.getActionName()).thenReturn("index");
    Mockito.when(proxy.getNamespace()).thenReturn("/");

    ActionInvocation invocation = Mockito.mock(ActionInvocation.class);
    Mockito.when(invocation.getProxy()).thenReturn(proxy);

    Map<String, Object> c = new HashMap<String, Object>();
    ActionContext ac = new ActionContext(c);
    ac.setActionInvocation(invocation);
    ActionContext.setContext(ac);

    IndexAction action = new IndexAction();
    String response = action.execute();
    assertEquals("successful response", "success", response);
  }

  @Test
  public void test404() throws Exception {

    ActionProxy proxy = Mockito.mock(ActionProxy.class);
    Mockito.when(proxy.getActionName()).thenReturn("something-else");
    Mockito.when(proxy.getNamespace()).thenReturn("/");

    ActionInvocation invocation = Mockito.mock(ActionInvocation.class);
    Mockito.when(invocation.getProxy()).thenReturn(proxy);

    Map<String, Object> c = new HashMap<String, Object>();
    ActionContext ac = new ActionContext(c);
    ac.setActionInvocation(invocation);
    ActionContext.setContext(ac);

    IndexAction action = new IndexAction();
    String response = action.execute();
    assertEquals("404 response", "NotFound", response);
  }

}
