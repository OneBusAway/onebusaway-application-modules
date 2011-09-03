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


import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContextManager implements ContextManager {

  private List<ContextListener> _listeners = new ArrayList<ContextListener>();

  private Context _context = new ContextImpl();

  public AbstractContextManager() {

  }

  @Override
  public void addContextListener(ContextListener listener) {
    _listeners.add(listener);
  }

  @Override
  public void removeContextListener(ContextListener listener) {
    _listeners.remove(listener);
  }

  @Override
  public Context getContext() {
    return _context;
  }

  protected void setInternalContext(Context context) {
    _context = context;
  }

  protected void fireContextChanged(Context context) {
    _context = context;
    for (ContextListener listener : _listeners)
      listener.onContextChanged(context);
  }
}
