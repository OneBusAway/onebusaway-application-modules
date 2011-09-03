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
package org.onebusaway.transit_data_federation.impl.time;

import java.util.List;

/**
 * Generic binary search that can accepts lists of Java objects that can be
 * adapted to a double value for searching.
 * 
 * @author bdferris
 * 
 */
public class GenericBinarySearch {

  /**
   * Return an index into the element list such that if a new element with the
   * specified target value was inserted into the list at the specified index,
   * the list would remain in sorted order with respect to the
   * {@link ValueAdapter}
   * 
   * @param elements a list of objects, sorted in the order appropriate to the
   *          {@link ValueAdapter}
   * @param targetValue target value to search for
   * @param valueAdapter adapter to convert the input element type into a double
   *          value
   * @return
   */
  public static <T> int search(List<T> elements, double targetValue,
      ValueAdapter<T> valueAdapter) {
    return search(elements, targetValue, valueAdapter, 0, elements.size());
  }

  /**
   * Return an index into the element list such that if a new element with the
   * specified target value was inserted into the list at the specified index,
   * the list would remain in sorted order with respect to the
   * {@link ValueAdapter}
   * 
   * @param elements a list of objects, sorted in the order appropriate to the
   *          {@link ValueAdapter}
   * @param targetValue target value to search for
   * @param valueAdapter adapter to convert the input element type into a double
   *          value
   * @return
   */
  public static <T> int search(T elements, int size, double targetValue,
      IndexAdapter<T> valueAdapter) {
    return search(elements, targetValue, valueAdapter, 0, size);
  }

  /**
   * Return an index into the element list such that if a new element with the
   * specified target value was inserted into the list at the specified index,
   * the list would remain in sorted order with respect to the
   * {@link IndexAdapter}
   * 
   * @param elements a collection of objects, sorted in the order appropriate to
   *          the {@link IndexAdapter}
   * @param indexFrom starting index range
   * @param indexTo ending index range
   * @param targetValue target value to search for
   * @param valueAdapter adapter to convert the input element type into a double
   *          value
   * @return
   */
  public static <T> int searchRange(T elements, int indexFrom, int indexTo,
      double targetValue, IndexAdapter<T> valueAdapter) {
    return search(elements, targetValue, valueAdapter, indexFrom, indexTo);
  }

  public interface ValueAdapter<T> {
    public double getValue(T value);
  }

  public interface IndexAdapter<T> {
    public double getValue(T source, int index);
  }

  /****
   * Private Methods
   ****/

  private static <T> int search(List<T> elements, double target,
      ValueAdapter<T> comparator, int fromIndex, int toIndex) {

    if (fromIndex == toIndex)
      return fromIndex;

    int midIndex = (fromIndex + toIndex) / 2;
    T element = elements.get(midIndex);
    double v = comparator.getValue(element);

    if (target < v) {
      return search(elements, target, comparator, fromIndex, midIndex);
    } else if (target > v) {
      return search(elements, target, comparator, midIndex + 1, toIndex);
    } else {
      return midIndex;
    }
  }

  private static <T> int search(T elements, double target,
      IndexAdapter<T> adapter, int fromIndex, int toIndex) {

    if (fromIndex == toIndex)
      return fromIndex;

    int midIndex = (fromIndex + toIndex) / 2;
    double v = adapter.getValue(elements, midIndex);

    if (target < v) {
      return search(elements, target, adapter, fromIndex, midIndex);
    } else if (target > v) {
      return search(elements, target, adapter, midIndex + 1, toIndex);
    } else {
      return midIndex;
    }
  }
}
