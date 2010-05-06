package org.onebusaway.utility;

import java.util.Iterator;
import java.util.SortedMap;

public class InterpolationLibrary {

  private static final String OUT_OF_RANGE = "attempt to interpolate key outside range of key-value data";

  private static final NumberInterpolationStrategy _numberInterpolation = new NumberInterpolationStrategy();

  public enum EOutOfRangeStrategy {
    INTERPOLATE, LAST_VALUE, EXCEPTION;
  }

  public static <K extends Number, V extends Number> double interpolate(
      SortedMap<K, V> values, K target) {
    return interpolate(values, target, EOutOfRangeStrategy.INTERPOLATE);
  }

  public static <K extends Number, V extends Number> double interpolate(
      SortedMap<K, V> values, K target, EOutOfRangeStrategy outOfRangeStrategy) {
    Number result = interpolate(_numberInterpolation, outOfRangeStrategy,
        values, target);
    return result.doubleValue();
  }

  public static double interpolatePair(double fromValue, double toValue,
      double ratio) {
    return ratio * (toValue - fromValue) + fromValue;
  }

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
