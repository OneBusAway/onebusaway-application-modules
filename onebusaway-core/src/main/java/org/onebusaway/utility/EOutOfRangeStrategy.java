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
package org.onebusaway.utility;

/**
 * Defines the strategy to use when interpolating a key that is outside the key
 * range of the key-value map
 * 
 * @author bdferris
 */
public enum EOutOfRangeStrategy {

  /**
   * As long as two key-values are present in the map, we we will attempt to
   * interpolate the value for a key that is outside the key range of the
   * key-value map. If only one key-value pair is present in the map, that value
   * will be used.
   */
  INTERPOLATE,

  /**
   * The closest key-value pair to target key outside the range of the key-value
   * map will be used. This usually corresponds to the first or last key-value
   * pair in the map depending on which end of the key-range the target key is
   * out-of-bounds.
   */
  LAST_VALUE,

  /**
   * An exception will be thrown when attempting to interpolate a key that is
   * outside the key range of the key-value map
   */
  EXCEPTION;
}