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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/closest-stop-for-vehicle.action")
public class ClosestStopForVehicleController {

  @Autowired
  private TripDetailsBeanService _service;

  @RequestMapping()
  public ModelAndView index(@RequestParam() String vehicleId,
      @RequestParam() long time, @RequestParam(required=false) String format) {
    
    AgencyAndId vid = AgencyAndIdLibrary.convertFromString(vehicleId);
    
    if( time == 0)
      time = SystemTime.currentTimeMillis();
    if( time < 0)
      time = SystemTime.currentTimeMillis() - time * 1000;
    
    TripDetailsInclusionBean inclusion = new TripDetailsInclusionBean();
    inclusion.setIncludeTripBean(false);
    inclusion.setIncludeTripSchedule(false);
    inclusion.setIncludeTripStatus(true);
    
    TripDetailsBean details = _service.getTripForVehicle(vid, time,inclusion);
    
    if( "html".equals(format))
      return new ModelAndView("closest-stop-for-vehicle-html.jspx", "stopTime", details);
    else
      return new ModelAndView("closest-stop-for-vehicle-xml.jspx", "stopTime", details);
  }
}
