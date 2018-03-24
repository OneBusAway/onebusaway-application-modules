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
package org.onebusaway.transit_data.model;

public class TransitDataConstants {

  public static final String STOP_GROUPING_TYPE_DIRECTION = "direction";

  // changed spelling/case to bring in line with GTFS-RT ScheduledRelationship
  public static final String STATUS_CANCELED = "CANCELED";

  public static final String STATUS_LEGACY_CANCELLED = "cancelled";
  
  public static final String STATUS_REROUTE = "reroute";
}
