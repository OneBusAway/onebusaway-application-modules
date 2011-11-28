/**
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.services.blocks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This decorator type is used to track all classes and interfaces the provide
 * or implement any sort of time-based index over transit data, whether it be
 * blocks, trips or stops.
 * 
 * @author bdferris
 * 
 */
@Target(value = ElementType.TYPE)
public @interface TransitTimeIndex {

}
