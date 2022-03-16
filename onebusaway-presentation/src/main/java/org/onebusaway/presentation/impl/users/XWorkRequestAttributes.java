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
package org.onebusaway.presentation.impl.users;

import java.util.HashMap;
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

  @Override
  public Object resolveReference(String key) {
    if (REFERENCE_REQUEST.equals(key)) {
      return getScopedMap(SCOPE_REQUEST);
    } else if (REFERENCE_SESSION.equals(key)) {
      return getScopedMap(SCOPE_SESSION);
    } else {
      return null;
    }
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
        Map<String, Object> map = new HashMap<>();
        for (String key : _context.getParameters().keySet()) {
          map.put(key, _context.getParameters().get(key).getValue());
        }
        return map;
      case SCOPE_SESSION:
        return _context.getSession();
      default:
        throw new IllegalStateException("unknown scope=" + scope);
    }
  }

}
