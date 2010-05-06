/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.common.web.common.client.context;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

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

  public void setContext(Object... params) {
    setContext(getParamsAsMap(params));
  }

  public void setContext(Map<String, String> params) {
    setContext(new ContextImpl(params));
  }

  public void setContext(Context context) {
    _manager.setContext(context);
  }

  public ClickListener getContextClickListener(Object... params) {
    return getContextClickListener(getParamsAsMap(params));
  }

  public ClickListener getContextClickListener(Map<String, String> params) {
    return getContextClickListener(new ContextImpl(params));
  }

  public ClickListener getContextClickListener(Context context) {
    return new ClickHandler(context);
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private Map<String, String> getParamsAsMap(Object... params) {
    if (params.length % 2 != 0)
      throw new IllegalArgumentException("Number of params must be even (key-value pairs)");
    Map<String, String> p = new LinkedHashMap<String, String>();
    for (int i = 0; i < params.length; i += 2)
      p.put(params[i].toString(), params[i + 1].toString());
    return p;
  }

  private class ClickHandler implements ClickListener {

    private Context _context;

    public ClickHandler(Context context) {
      _context = context;
    }

    public void onClick(Widget arg0) {
      setContext(_context);
    }
  }
}
