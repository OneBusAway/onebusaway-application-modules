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

import java.util.LinkedHashMap;
import java.util.Map;

public class ContextHelper {

  private final ContextManager _manager;

  public ContextHelper() {
    this(new DirectContextManager());
  }

  public ContextHelper(ContextManager manager) {
    _manager = manager;
  }
  
  public ContextManager getManager() {
    return _manager;
  }

  public void setContext(Object... params) {
    setContext(getParamsAsMap(params));
  }

  public void setContext(Map<String, String> params) {
    setContext(new ContextImpl(params));
  }

  public void setContext(Context context) {
    _manager.setContext(context);
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private Map<String, String> getParamsAsMap(Object... params) {
    if (params.length % 2 != 0)
      throw new IllegalArgumentException(
          "Number of params must be even (key-value pairs)");
    Map<String, String> p = new LinkedHashMap<String, String>();
    for (int i = 0; i < params.length; i += 2)
      p.put(params[i].toString(), params[i + 1].toString());
    return p;
  }
}
