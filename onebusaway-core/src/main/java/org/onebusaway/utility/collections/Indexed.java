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
package org.onebusaway.utility.collections;

import java.io.Serializable;

/**
 * A wrapper class for an object that add an integer index parameter that can be
 * used to apply a {@link Comparable} sort order to an arbitrary type.
 * 
 * @author bdferris
 * 
 * @param <T>
 */
public class Indexed<T> implements Comparable<Indexed<T>>, Serializable {

  private static final long serialVersionUID = 1L;

  private T _value;

  private int _index;

  public static <TO> Indexed<TO> create(TO value, int index) {
    return new Indexed<TO>(value, index);
  }

  public Indexed(T value, int index) {
    _value = value;
    _index = index;
  }

  public T getValue() {
    return _value;
  }

  public int getIndex() {
    return _index;
  }

  public int compareTo(Indexed<T> o) {
    return _index == o._index ? 0 : (_index < o._index ? -1 : 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Indexed<?>))
      return false;
    Indexed<?> o = (Indexed<?>) obj;
    return _index == o._index && _value.equals(o._value);
  }

  @Override
  public int hashCode() {
    return _index + _value.hashCode();
  }

  @Override
  public String toString() {
    return _value.toString() + "[" + _index + "]";
  }
}