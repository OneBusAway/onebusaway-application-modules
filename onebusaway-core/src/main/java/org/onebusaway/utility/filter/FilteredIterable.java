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

/**
 * An iterable adapter that selectively filters a source {@link Iterable} given
 * a {@link IFilter} to apply.
 * 
 * @author bdferris
 */
public class FilteredIterable<T> implements Iterable<T> {

  private Iterable<T> _source;

  private IFilter<T> _filter;

  public static <T> Iterable<T> filter(Iterable<T> source, IFilter<T> filter) {
    return new FilteredIterable<T>(source, filter);
  }

  public FilteredIterable(Iterable<T> source, IFilter<T> filter) {
    _source = source;
    _filter = filter;
  }

  public Iterator<T> iterator() {
    return new FilteredIterator<T>(_source.iterator(), _filter);
  }

}
