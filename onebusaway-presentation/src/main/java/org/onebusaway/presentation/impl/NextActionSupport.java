package org.onebusaway.presentation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
      Map<String, Object> contextParameters = context.getParameters();
      contextParameters.putAll(params);
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
