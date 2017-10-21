/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.service;

import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs_realtime.model.VehiclePositionModel;

public interface VehiclePositionDao {

  void saveOrUpdate(VehiclePositionModel... array);

  List<String> getAllVehicleIds();

  List<VehiclePositionModel> getVehiclePositions(String vehicleId,
      Date startDate, Date endDate);
  
  List<VehiclePositionModel> findByDate(Date startDate, Date endDate);
}
