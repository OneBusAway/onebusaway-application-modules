/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;

/**
 * Service methods for accessing the list of stop times for a specified trip.
 * Here, {@link TripStopTimesBean} and {@link TripStopTimeBean} serve as
 * high-level descriptors of low level {@link Trip} and {@link StopTime}
 * objects.
 * 
 * @author bdferris
 * 
 */
public interface TripStopTimesBeanService {

  public TripStopTimesBean getStopTimesForBlockTrip(BlockTripInstance blockTripInstance);
}
