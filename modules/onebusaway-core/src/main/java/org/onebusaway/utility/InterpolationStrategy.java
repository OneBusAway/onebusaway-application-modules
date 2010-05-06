package org.onebusaway.utility;

public interface InterpolationStrategy<KEY extends Number, VALUE> {
  public VALUE interpolate(KEY prevKey, VALUE prevValue, KEY nextKey, VALUE nextValue, double ratio);
}
