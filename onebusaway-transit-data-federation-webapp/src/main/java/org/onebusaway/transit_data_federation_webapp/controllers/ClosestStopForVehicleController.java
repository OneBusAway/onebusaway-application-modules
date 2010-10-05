package org.onebusaway.transit_data_federation_webapp.controllers;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
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
      time = System.currentTimeMillis();
    if( time < 0)
      time = System.currentTimeMillis() - time * 1000;
    
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
