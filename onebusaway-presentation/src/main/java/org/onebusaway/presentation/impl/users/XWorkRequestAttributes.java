package org.onebusaway.presentation.impl.users;

import java.util.Map;
import java.util.Set;

import org.springframework.web.context.request.AbstractRequestAttributes;

import com.opensymphony.xwork2.ActionContext;

public class XWorkRequestAttributes extends AbstractRequestAttributes {

  private ActionContext _context;

  private String _sessionId;

  public XWorkRequestAttributes(ActionContext context, String sessionId) {
    _context = context;
    _sessionId = sessionId;
  }

  @Override
  public String[] getAttributeNames(int scope) {
    Map<String, Object> attrs = getScopedMap(scope);
    Set<String> keys = attrs.keySet();
    String[] names = new String[keys.size()];
    int index = 0;
    for (String name : keys)
      names[index++] = name;
    return names;
  }

  @Override
  public Object getAttribute(String name, int scope) {
    return getScopedMap(scope).get(name);
  }

  @Override
  public void removeAttribute(String name, int scope) {
    getScopedMap(scope).remove(name);
  }

  @Override
  public void setAttribute(String name, Object value, int scope) {
    getScopedMap(scope).put(name, value);
  }

  @Override
  public String getSessionId() {
    return _sessionId;
  }

  @Override
  public Object getSessionMutex() {
    return getScopedMap(SCOPE_SESSION);
  }

  @Override
  public void registerDestructionCallback(String name, Runnable callback,
      int scope) {
    
  }

  /****
   * Private Methods
   ****/

  @Override
  protected void updateAccessedSessionAttributes() {
    
  }

  private Map<String, Object> getScopedMap(int scope) {
    switch (scope) {
      case SCOPE_REQUEST:
        return _context.getParameters();
      case SCOPE_SESSION:
        return _context.getSession();
      case SCOPE_GLOBAL_SESSION:
        return _context.getApplication();
      default:
        throw new IllegalStateException("unknown scope=" + scope);
    }
  }
}
