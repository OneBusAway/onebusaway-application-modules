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