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
package org.onebusaway.utility.filter;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator adapter that selectively filters a source {@link Iterator} given
 * a {@link IFilter} to apply.
 * 
 * @author bdferris
 */
public class FilteredIterator<T> implements Iterator<T> {

  private Iterator<T> _sourceIt;

  private IFilter<T> _filter;

  private T _next;

  public FilteredIterator(Iterator<T> sourceIt, IFilter<T> filter) {
    _sourceIt = sourceIt;
    _filter = filter;
    _next = getNext();
  }

  public boolean hasNext() {
    return _next != null;
  }

  public T next() {
    if (!hasNext())
      throw new NoSuchElementException();
    T next = _next;
    _next = getNext();
    return next;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  private T getNext() {
    while (_sourceIt.hasNext()) {
      T obj = _sourceIt.next();
      if (_filter.isEnabled(obj))
        return obj;
    }
    return null;
  }
}