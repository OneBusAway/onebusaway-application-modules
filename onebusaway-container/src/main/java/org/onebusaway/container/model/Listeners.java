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
package org.onebusaway.container.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Convenience container for a collection of typed listener objects
 * 
 * @author bdferris
 * @see HasListeners
 */
public class Listeners<T> implements Iterable<T>, HasListeners<T> {

  private List<T> _listeners = new ArrayList<T>();

  public void addListener(T listener) {
    _listeners.add(listener);
  }

  public void removeListener(T listener) {
    _listeners.remove(listener);
  }

  @Override
  public Iterator<T> iterator() {
    return _listeners.iterator();
  }
}
