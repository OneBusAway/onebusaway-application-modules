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
package org.onebusaway.gtfs_realtime.archiver.controller;

import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs_realtime.archiver.service.VehiclePositionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.onebusaway.gtfs_realtime.model.VehiclePositionModel;

@Controller
public class VehiclePositionController {

  private VehiclePositionDao _vehiclePositionDao;

  @Autowired
  public void setVehiclePositionDao(VehiclePositionDao dao) {
    _vehiclePositionDao = dao;
  }

  @RequestMapping(value = "/vehicleIds")
  public @ResponseBody List<String> getVehicleIds() {
    return _vehiclePositionDao.getAllVehicleIds();
  }

  @RequestMapping(value = "/vehiclePositions")
  public @ResponseBody List<VehiclePositionModel> getVehiclePositions(
      @RequestParam(value = "vehicleId") String vehicleId,
      @RequestParam(value = "startDate", required = false, defaultValue = "-1") long start,
      @RequestParam(value = "endDate", required = false, defaultValue = "-1") long end) {

    // startDate and endDate are null if not present in request params.
    Date startDate = (start > 0) ? new Date(start) : null,
        endDate = (end > 0) ? new Date(end) : null;

    return _vehiclePositionDao.getVehiclePositions(vehicleId, startDate,
        endDate);
  }

}
