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
package org.onebusaway.presentation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts2.dispatcher.HttpParameters;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.model.NextAction;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public abstract class NextActionSupport extends ActionSupport implements
    SessionAware {

  private static final long serialVersionUID = 1L;

  private static final String NEXT_ACTION_STACK_SESSION_KEY = NextActionSupport.class.getName()
      + ".nextActionStack";

  protected Map<String, Object> _session;

  public void setSession(Map<String, Object> session) {
    _session = session;
  }

  /****
   * Protected Methods
   ****/
  
  protected void clearNextActions() {
    List<NextAction> stack = getNextActionStack(false);
    if( stack != null)
      stack.clear();
    stack = getNextActionStack(false);
  }

  protected String getNextActionOrSuccess() {
    List<NextAction> stack = getNextActionStack(false);
    if (stack == null || stack.isEmpty())
      return SUCCESS;

    NextAction next = stack.remove(stack.size() - 1);

    Map<String, String[]> params = next.getParameters();
    if (params != null && !params.isEmpty()) {
      ActionContext context = ActionContext.getContext();
      HttpParameters parameters = context.getParameters();
      Map<String, Object> contextParameters = new HashMap<>();
      for (String key : parameters.keySet()) {
        contextParameters.put(key, parameters.get(key).getValue());
      }
    }

    return next.getAction();
  }

  protected void pushNextAction(String action) {
    List<NextAction> stack = getNextActionStack(true);
    stack.add(new NextAction(action));
  }

  protected void pushNextAction(String action, String key, String value) {
    List<NextAction> stack = getNextActionStack(true);
    stack.add(new NextAction(action, key, value));
  }

  /****
   * Private Methods
   ****/

  @SuppressWarnings("unchecked")
  private List<NextAction> getNextActionStack(boolean create) {
    List<NextAction> stack = (List<NextAction>) _session.get(NEXT_ACTION_STACK_SESSION_KEY);
    if (stack == null && create) {
      stack = new ArrayList<NextAction>();
      _session.put(NEXT_ACTION_STACK_SESSION_KEY, stack);
    }
    return stack;
  }
}
