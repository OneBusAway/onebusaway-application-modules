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