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
package org.onebusaway.transit_data_federation.impl.realtime;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Track usage of vehicleLocationListener.
 */
public class TestVehicleLocationListener implements VehicleLocationListener {
  private List<VehicleLocationRecord> _records = new ArrayList<>();
  private VehicleLocationListener _listener;
  public void setVehicleLocationListener(VehicleLocationListener listener) {
    _listener = listener;
  }
  @Override
  public void handleVehicleLocationRecord(VehicleLocationRecord record) {
    _records.add(record);
    _listener.handleVehicleLocationRecord(record);
  }

  @Override
  public void handleVehicleLocationRecords(List<VehicleLocationRecord> records) {
    _records.addAll(records);
    _listener.handleVehicleLocationRecords(records);
  }

  @Override
  public void resetVehicleLocation(AgencyAndId vehicleId) {
    _listener.resetVehicleLocation(vehicleId);
  }

  @Override
  public void handleRawPosition(AgencyAndId vehicleId, double lat, double lon, long timestamp) {

  }

  public List<VehicleLocationRecord> getRecords() {
    return _records;
  }

  public void reset() {
    _records.clear();
  }

}
