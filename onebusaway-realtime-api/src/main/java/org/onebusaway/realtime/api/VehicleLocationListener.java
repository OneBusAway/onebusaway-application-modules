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
package org.onebusaway.realtime.api;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * A vehicle location listener is a component that can listen to
 * {@link VehicleLocationRecord} records incoming from an AVL data source.
 * 
 * @author bdferris
 * @see VehicleLocationRecord
 */
public interface VehicleLocationListener {
  
  public void handleVehicleLocationRecord(VehicleLocationRecord record);

  public void handleVehicleLocationRecords(List<VehicleLocationRecord> records);
  
  public void resetVehicleLocation(AgencyAndId vehicleId);

  public void handleRawPosition(AgencyAndId vehicleId, double lat, double lon, long timestamp);

}
