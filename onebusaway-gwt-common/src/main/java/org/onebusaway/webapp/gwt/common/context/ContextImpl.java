package org.onebusaway.webapp.gwt.common.context;

import java.util.HashMap;
import java.util.Map;

/*****************************************************************************
 * 
 ****************************************************************************/

public class ContextImpl implements Context {

  private Map<String, String> _params;

  public ContextImpl() {
    this(new HashMap<String, String>());
  }

  public ContextImpl(Map<String, String> params) {
    _params = params;
  }

  public boolean hasParam(String name) {
    return _params.containsKey(name);
  }

  public String getParam(String name) {
    return _params.get(name);
  }

  public Map<String, String> getParams() {
    return _params;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ContextImpl))
      return false;
    ContextImpl other = (ContextImpl) obj;
    return _params.equals(other._params);
  }

  @Override
  public int hashCode() {
    return _params.hashCode();
  }

  @Override
  public String toString() {
    return "Context(" + _params.toString() + ")";
  }

}