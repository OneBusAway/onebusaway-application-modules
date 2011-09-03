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
package org.onebusaway.transit_data.model.tripplanning;

/**
 * Why is this a string and not an enum? To allow for the future addition of new
 * modes without a recompile. These string keys are offered mostly for
 * convenience.
 * 
 * @author bdferris
 */
public class Modes {

  public static final String WALK = "walk";

  public static final String TRANSIT = "transit";
}
