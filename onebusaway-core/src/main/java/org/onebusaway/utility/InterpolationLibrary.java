/*
 * Copyright 2008-2010 Brian Ferris
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
package org.onebusaway.utility;

import java.util.Iterator;
import java.util.SortedMap;

/**
 * Generic methods to support interpolation of values against a sorted key-value
 * map given a new target key.
 * 
 * @author bdferris
 */
public class InterpolationLibrary {

  private static final String OUT_OF_RANGE = "attempt to interpolate key outside range of key-value data";

  private static final NumberInterpolationStrategy _numberInterpolation = new NumberInterpolationStrategy();

  /**
   * Same behavior as
   * {@link #interpolate(SortedMap, Number, EOutOfRangeStrategy) but with a
   * default {@link EOutOfRangeStrategy} of
   * {@link EOutOfRangeStrategy#INTERPOLATE}.
   * 
   * @param values a sorted-map of key-value number pairs
   * @param target the target key used to interpolate a value
   * @param outOfRangeStrategy the strategy to use for a target key that outside
   *          the key-range of the value map
   * @return an interpolated value for the target key
   */
  public static <K extends Number, V extends Number> double interpolate(
      SortedMap<K, V> values, K target) {
    return interpolate(values, target, EOutOfRangeStrategy.INTERPOLATE);
  }

  /**
   * Given a {@link SortedMap} with key-values that all extend from
   * {@link Number}, interpolate using linear interpolation a value for a target
   * key within the key-range of the map. For a key outside the range of the
   * keys of the map, the {@code outOfRange} {@link EOutOfRangeStrategy}
   * strategy will determine the interpolation behavior.
   * 
   * @param values a sorted-map of key-value number pairs
   * @param target the target key used to interpolate a value
   * @param outOfRangeStrategy the strategy to use for a target key that outside
   *          the key-range of the value map
   * @return an interpolated value for the target key
   */
  public static <K extends Number, V extends Number> double interpolate(
      SortedMap<K, V> values, K target, EOutOfRangeStrategy outOfRangeStrategy) {
    Number result = interpolate(_numberInterpolation, outOfRangeStrategy,
        values, target);
    return result.doubleValue();
  }

  /**
   * Simple numeric interpolation between two double values using the equation
   * {@code ratio * (toValue - fromValue) + fromValue}
   * 
   * @param fromValue
   * @param toValue
   * @param ratio
   * @return {@code ratio * (toValue - fromValue) + fromValue}
   */
  public static double interpolatePair(double fromValue, double toValue,
      double ratio) {
    return ratio * (toValue - fromValue) + fromValue;
  }

  /**
   * Given a {@link SortedMap} with key-values that all extend from
   * {@link Number}, interpolate using linear interpolation a value for a target
   * key within the key-range of the map. For a key outside the range of the
   * keys of the map, the {@code outOfRange} {@link EOutOfRangeStrategy}
   * strategy will determine the interpolation behavior.
   * 
   * @param values
   * @param target the target key used to interpolate a value
   * @param outOfRangeStrategy the strategy to use for a target key that outside
   *          the key-range of the value map
   * @return an interpolated value for the target key
   */

  /**
   * 
   * Given a {@link SortedMap} with key-values that of arbitrary type and a
   * {@link InterpolationStrategy} to define interpolation over those types,
   * interpolate a value for a target key within the key-range of the map. For a
   * key outside the range of the keys of the map, the {@code outOfRange}
   * {@link EOutOfRangeStrategy} strategy will determine the interpolation
   * behavior.
   * 
   * @param interpolationStrategy the interpolation strategy used to perform
   *          interpolation between key-value pairs of arbitrary type
   * @param outOfRangeStrategy the strategy to use for a target key that outside
   *          the key-range of the value map
   * @param values a sorted-map of key-value pairs
   * @param target the target key used to interpolate a value
   * @return an interpolated value for the target key
   */
  public static <KEY extends Number, VALUE, ANY_KEY extends KEY, ANY_VALUE extends VALUE> VALUE interpolate(
      InterpolationStrategy<KEY, VALUE> interpolationStrategy,
      EOutOfRangeStrategy outOfRangeStrategy,
      SortedMap<ANY_KEY, ANY_VALUE> values, ANY_KEY target) {

    if (values.containsKey(target))
      return values.get(target);

    SortedMap<ANY_KEY, ANY_VALUE> before = values.headMap(target);
    SortedMap<ANY_KEY, ANY_VALUE> after = values.tailMap(target);

    ANY_KEY prevKey = null;
    ANY_KEY nextKey = null;

    if (before.isEmpty()) {

      if (after.isEmpty())
        throw new IndexOutOfBoundsException(OUT_OF_RANGE);

      switch (outOfRangeStrategy) {
        case INTERPOLATE:
          if (after.size() == 1)
            return after.get(after.firstKey());
          Iterator<ANY_KEY> it = after.keySet().iterator();
          prevKey = it.next();
          nextKey = it.next();
          break;
        case LAST_VALUE:
          return after.get(after.firstKey());
        case EXCEPTION:
          throw new IndexOutOfBoundsException(OUT_OF_RANGE);
      }
    } else if (after.isEmpty()) {

      if (before.isEmpty())
        throw new IndexOutOfBoundsException(OUT_OF_RANGE);

      switch (outOfRangeStrategy) {
        case INTERPOLATE:
          if (before.size() == 1)
            return before.get(before.lastKey());
          nextKey = before.lastKey();
          before = before.headMap(nextKey);
          prevKey = before.lastKey();
          break;
        case LAST_VALUE:
          return before.get(before.lastKey());
        case EXCEPTION:
          throw new IndexOutOfBoundsException(OUT_OF_RANGE);
      }
    } else {
      prevKey = before.lastKey();
      nextKey = after.firstKey();
    }

    VALUE prevValue = values.get(prevKey);
    VALUE nextValue = values.get(nextKey);

    double keyRatio = (target.doubleValue() - prevKey.doubleValue())
        / (nextKey.doubleValue() - prevKey.doubleValue());

    VALUE result = interpolationStrategy.interpolate(prevKey, prevValue,
        nextKey, nextValue, keyRatio);
    return result;
  }

  private static class NumberInterpolationStrategy implements
      InterpolationStrategy<Number, Number> {

    @Override
    public Number interpolate(Number prevKey, Number prevValue, Number nextKey,
        Number nextValue, double ratio) {

      double result = interpolatePair(prevValue.doubleValue(),
          nextValue.doubleValue(), ratio);
      return new Double(result);
    }
  }
}
