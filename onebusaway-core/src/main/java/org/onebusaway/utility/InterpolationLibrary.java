/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2015 University of South Florida
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
package org.onebusaway.utility;

import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;

/**
 * Generic methods to support interpolation of values against a sorted key-value
 * map given a new target key.
 * 
 * Note that these interpolation methods do not conform to the GTFS-rt spec.  For GTFS-rt
 * compliant interpolation/extrapolation, see {@link TransitInterpolationLibrary}.
 * 
 * @author bdferris
 */
public class InterpolationLibrary {

  private static final String OUT_OF_RANGE = "attempt to interpolate key outside range of key-value data";

  private static final NumberInterpolationStrategy _numberInterpolation = new NumberInterpolationStrategy();

  /**
   * Same behavior as
   * {@link #interpolate(SortedMap, Number, EOutOfRangeStrategy)} but with a
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

  public static <K extends Number, V> V nearestNeighbor(SortedMap<K, V> values,
      K target) {
    if (values.isEmpty())
      return null;
    SortedMap<K, V> before = values.headMap(target);
    SortedMap<K, V> after = values.tailMap(target);
    if (before.isEmpty()) {
      return after.get(after.firstKey());
    } else if (after.isEmpty()) {
      return before.get(before.lastKey());
    } else {
      K a = before.lastKey();
      K b = after.firstKey();
      if (Math.abs(b.doubleValue() - target.doubleValue()) < Math.abs(a.doubleValue()
          - target.doubleValue())) {
        return after.get(b);
      } else {
        return before.get(a);
      }
    }
  }

  public static double interpolate(double[] keys, double[] values,
	      double target, EOutOfRangeStrategy outOfRangeStrategy) {
	  return interpolate(keys, values, target, outOfRangeStrategy, null);
  }

  public static double interpolate(double[] keys, double[] values,
      double target, EOutOfRangeStrategy outOfRangeStrategy, EInRangeStrategy inRangeStrategy) {

    if (values.length == 0)
      throw new IndexOutOfBoundsException(OUT_OF_RANGE);

    int index = Arrays.binarySearch(keys, target);
    if (index >= 0)
      return values[index];

    index = -(index + 1);

    if (index == values.length) {
      switch (outOfRangeStrategy) {
        case INTERPOLATE:
          if (values.length > 1)
            return interpolatePair(keys[index - 2], values[index - 2],
                keys[index - 1], values[index - 1], target);
          return values[index - 1];
        case LAST_VALUE:
          return values[index - 1];
        case EXCEPTION:
          throw new IndexOutOfBoundsException(OUT_OF_RANGE);
      }
    }

    if (index == 0) {
      switch (outOfRangeStrategy) {
        case INTERPOLATE:
          if (values.length > 1)
            return interpolatePair(keys[0], values[0], keys[1], values[1],
                target);
          return values[0];
        case LAST_VALUE:
          return values[0];
        case EXCEPTION:
          throw new IndexOutOfBoundsException(OUT_OF_RANGE);
      }
    }

    if (inRangeStrategy == null) {
    	inRangeStrategy = EInRangeStrategy.INTERPOLATE;
    }

    switch (inRangeStrategy) {
    case PREVIOUS_VALUE:
    	return values[index - 1];
    default:
    	return interpolatePair(keys[index - 1], values[index - 1], keys[index],
    			values[index], target);
    }
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
   * Simple numeric interpolation between two pairs of key-values and a third
   * key. Here, {@code ratio = (targetKey - keyA) / (keyB - keyA)} and the
   * result is {@code ratio * (valueB - valueA) + valueA}.
   * 
   * @param fromValue
   * @param toValue
   * @param ratio
   * @return {@code ratio * (toValue - fromValue) + fromValue}
   */
  public static double interpolatePair(double keyA, double valueA, double keyB,
      double valueB, double targetKey) {
    double ratio = (targetKey - keyA) / (keyB - keyA);
    return interpolatePair(valueA, valueB, ratio);
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
