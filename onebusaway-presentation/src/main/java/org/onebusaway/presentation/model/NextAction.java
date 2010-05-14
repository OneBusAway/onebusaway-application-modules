/**
 * 
 */
package org.onebusaway.presentation.model;

import java.util.HashMap;
import java.util.Map;

public class NextAction {

  private final String _action;

  private final Map<String, String[]> _parameters;

  public NextAction(String action) {
    this(action, new HashMap<String, String[]>());
  }

  public NextAction(String action, String key, String value) {
    this(action, getMap(key, value));
  }

  public NextAction(String action, Map<String, String[]> parameters) {
    _action = action;
    _parameters = parameters;
  }

  public String getAction() {
    return _action;
  }

  public Map<String, String[]> getParameters() {
    return _parameters;
  }

  private static Map<String, String[]> getMap(String key, String value) {
    Map<String, String[]> params = new HashMap<String, String[]>();
    params.put(key, new String[] {value});
    return params;
  }

  @Override
  public String toString() {
    return "NextAction(action=" + _action + " params=" + _parameters+")";
  }  
}