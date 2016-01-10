/**
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

/**
 * Transit-specific methods to support searching for deviations (produced from real-time 
 * predictions) for a given stop.  Interpolation behavior is consistent 
 * with the GTFS-realtime spec (https://developers.google.com/transit/gtfs-realtime/) when
 * using the {@link EInRangeStrategy.PREVIOUS_VALUE} and {@link EOutOfRangeStrategy.LAST_VALUE}
 * strategies - in particular, this applies to the propagation of delays downstream in a trip.
 * The {@link EInRangeStrategy.INTERPOLATE} and {@link EOutOfRangeStrategy.INTERPOLATE}
 * strategies have behavior consistent with the normal {@link InterpolationLibrary}, which
 * do not conform to the GTFS-rt spec.
 * 
 */
public class TransitInterpolationLibrary {

  private static final String OUT_OF_RANGE = "no values provided";

  public static Double interpolate(double[] keys, double[] values,
      double target, EOutOfRangeStrategy outOfRangeStrategy) {
    return interpolate(keys, values, target, outOfRangeStrategy, null);
  }

  /**
   * Find the deviation that should be used for a particular stop, given sorted keys (arrival times)
   * and values (deviations) arrays.  The {@code target} is the arrival time for the stop
   * we're searching for.  Delay propagation is consistent with the GTFS-realtime spec 
   * (https://developers.google.com/transit/gtfs-realtime/) when using the 
   * {@link EInRangeStrategy.PREVIOUS_VALUE} and {@link EOutOfRangeStrategy.LAST_VALUE} strategies.  If
   *  {@link EOutOfRangeStrategy.LAST_VALUE} is provided and all deviations are downstream from the target stop, 
   * null will be returned to indicate that no real-time information is available for the target stop. 
   * If {@link EInRangeStrategy.INTERPOLATE} is provided, this method will interpolate using 
   * linear interpolation and produce a value for a target key within the key-range of the map. 
   * For a key outside the range of the keys of the map, the {@code outOfRange} {@link EOutOfRangeStrategy}
   * strategy will determine the interpolation behavior.  {@link EOutOfRangeStrategy.INTERPOLATE}
   * will linearly extrapolate the value.
   * 
   * @param keys sorted array of keys (the scheduled arrival time of the stop)
   * @param values sorted arrays of values (the list of real-time deviations for the provided stops) 
   * @param target the target key used to interpolate a value (the scheduled arrival time of the stop)
   * @param outOfRangeStrategy the strategy to use for a target key that outside
   *          the key-range of the value map (use {@link EOutOfRangeStrategy.LAST_VALUE} for GTFS-rt behavior)
   * @param inRangeStrategy the strategy to use for a target key that inside
   *          the key-range of the value map (use {@link EInRangeStrategy.PREVIOUS_VALUE} for GTFS-rt behavior)
   * @return an interpolated value (deviation) for the target key, or null if the target is upstream of the deviations
   */
  public static Double interpolate(double[] keys, double[] values,
      double target, EOutOfRangeStrategy outOfRangeStrategy,
      EInRangeStrategy inRangeStrategy) {

    if (values.length == 0)
      throw new IndexOutOfBoundsException(OUT_OF_RANGE);

    int index = Arrays.binarySearch(keys, target);
    if (index >= 0) {
      // There is a real-time prediction provided for this stop - return it
      return values[index];
    }

    // If we get this far, the target value wasn't contained in the keys.  Convert the returned index into the insertion 
    // index for target, which is the index of the first element greater than the target key (see Arrays.binarySearch()).
    index = -(index + 1);

    if (index == values.length) {
      // We're searching for a stop that is downstream of the predictions
      switch (outOfRangeStrategy) {
        case INTERPOLATE:
          if (values.length > 1)
            return InterpolationLibrary.interpolatePair(keys[index - 2],
                values[index - 2], keys[index - 1], values[index - 1], target);
          return values[index - 1];
        case LAST_VALUE:
          // Return the closest upstream deviation (i.e., propagate the last deviation in values downstream)
          return values[index - 1];
        case EXCEPTION:
          throw new IndexOutOfBoundsException(OUT_OF_RANGE);
      }
    }

    if (index == 0) {
      // We're searching for a stop that is upstream of the predictions
      switch (outOfRangeStrategy) {
        case INTERPOLATE:
          if (values.length > 1)
            return InterpolationLibrary.interpolatePair(keys[0], values[0],
                keys[1], values[1], target);
          return values[0];
        case LAST_VALUE:
          // We shouldn't propagate deviations upstream, so return null to indicate no prediction
          // should be used, and schedule data should be used instead.
          return null;
        case EXCEPTION:
          throw new IndexOutOfBoundsException(OUT_OF_RANGE);
      }
    }

    if (inRangeStrategy == null) {
      inRangeStrategy = EInRangeStrategy.INTERPOLATE;
    }

    // We're searching for a stop that is within the window of predictions, but no prediction is provided for
    // the target stop
    switch (inRangeStrategy) {
      case PREVIOUS_VALUE:
        // Return the closest upstream deviation (i.e., propagate the closest deviation in values downstream)
        return values[index - 1];
      default:
        return InterpolationLibrary.interpolatePair(keys[index - 1],
            values[index - 1], keys[index], values[index], target);
    }
  }

}
