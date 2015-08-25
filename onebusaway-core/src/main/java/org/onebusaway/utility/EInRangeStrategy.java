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

/**
 * Defines the strategy to use when interpolating a key that is inside the key
 * range of the key-value map
 */
public enum EInRangeStrategy {

  /**
   * As long as two key-values are present in the map, we we will attempt to
   * interpolate the value for a key that is inside the key range of the
   * key-value map. If only one key-value pair is present in the map, that value
   * will be used.
   */
  INTERPOLATE,

  /**
   * Returns the value in the key-value map closest to the target value, where the
   * index for the returned value is less than the index of the target value.
   */
  PREVIOUS_VALUE;
}