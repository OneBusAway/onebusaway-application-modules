/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtimeNYCT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of GTFS-RT added trip support.
 */
public class AddedTripServiceImpl implements AddedTripService {

  private static final Logger _log = LoggerFactory.getLogger(AddedTripServiceImpl.class);
  private NyctTripService nycService = new NyctTripServiceImpl();
  @Override
  public AddedTripInfo handleNyctDescriptor(GtfsRealtime.TripUpdate tu, GtfsRealtimeNYCT.NyctTripDescriptor nyctTripDescriptor,
                                            long currentTime) {
    AddedTripInfo info = nycService.parse(tu,nyctTripDescriptor, currentTime);
    return info;
  }
}
