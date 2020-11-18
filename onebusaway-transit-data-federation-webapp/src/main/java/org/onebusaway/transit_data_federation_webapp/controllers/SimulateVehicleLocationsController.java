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
package org.onebusaway.transit_data_federation_webapp.controllers;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateQueryBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateQueryBean.Record;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/simulate-vehicle-locations.action")
public class SimulateVehicleLocationsController {

  @Autowired
  private BlockLocationService _blockLocationService;

  @RequestMapping()
  public ModelAndView index(@RequestParam() String vehicleId,
      @RequestParam(defaultValue = "0.0") double noise) {

    AgencyAndId vid = AgencyAndIdLibrary.convertFromString(vehicleId);

    CurrentVehicleEstimateQueryBean bean = new CurrentVehicleEstimateQueryBean();

    long time = SystemTime.currentTimeMillis();
    
    List<Record> records = new ArrayList<Record>();

    for (int i = 0; i < 5 * 60; i += 30) {

      TargetTime tt = new TargetTime(time - i * 1000);
      BlockLocation location = _blockLocationService.getLocationForVehicleAndTime(
          vid, tt);

      if (location != null) {

        CoordinatePoint p = applyNoiseToLocation(location.getLocation(), noise);

        Record r = new Record();
        r.setLocation(location.getLocation());
        r.setTimestamp(tt.getTargetTime());
        r.setLocation(p);
        r.setAccuracy(noise);
        records.add(r);
      }
    }
    
    bean.setRecords(records);

    ModelAndView mv = new ModelAndView("simulate-vehicle-locations.jspx");
    mv.addObject("time", time);
    mv.addObject("query", bean);
    return mv;
  }

  private CoordinatePoint applyNoiseToLocation(CoordinatePoint location,
      double noise) {

    CoordinateBounds b = SphericalGeometryLibrary.bounds(location, noise);
    double latSpan = b.getMaxLat() - b.getMinLat();
    double lonSpan = b.getMaxLon() - b.getMinLon();
    double lat = b.getMinLat() + Math.random() * latSpan;
    double lon = b.getMinLon() + Math.random() * lonSpan;
    return new CoordinatePoint(lat, lon);
  }
}
