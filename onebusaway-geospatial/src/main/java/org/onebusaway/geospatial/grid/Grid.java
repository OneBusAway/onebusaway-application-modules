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
package org.onebusaway.geospatial.grid;

public interface Grid<T> {

  public void set(int x, int y, T element);

  public void set(GridIndex index, T element);

  public boolean contains(int x, int y);

  public boolean contains(GridIndex index);

  public T get(int x, int y);

  public T get(GridIndex index);

  public Iterable<Entry<T>> getEntries();

  public interface Entry<T> {
    public GridIndex getIndex();

    public T getValue();
  }
}
