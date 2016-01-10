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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

public class KCMStopModificationStrategy implements StopModificationStrategy {

  @Override
  public String convertStopId(String stopId) {
    if (stopId == null || stopId.length() == 0)
      return stopId;
    if (stopId.startsWith("100000")) {
      return stopId.replaceFirst("100000", "");
    }
    if (stopId.startsWith("10000")) {
      return stopId.replaceFirst("10000", "");
    }

    if (stopId.startsWith("1000")) {
      return stopId.replaceFirst("1000", "");
    }
    return stopId;
  }
}
