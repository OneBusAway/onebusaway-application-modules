package org.onebusaway.transit_data_federation_webapp.controllers;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.predictions.TripTimePredictionService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/closest-stop-for-vehicle.action")
public class ClosestStopForVehicleController {

  @Autowired
  private TripTimePredictionService _service;

  @RequestMapping()
  public ModelAndView index(@RequestParam() String vehicleId,
      @RequestParam() long time, @RequestParam(required=false) String format) {
    
    if( time == 0)
      time = System.currentTimeMillis();
    
    if( time < 0)
      time = System.currentTimeMillis() - time * 1000;
    
    AgencyAndId id = AgencyAndIdLibrary.convertFromString(vehicleId);
    StopTimeEntry entry = _service.getClosestStopForVehicleAndTime(id, time);
    if( "html".equals(format))
      return new ModelAndView("closest-stop-for-vehicle-html.jspx", "stopTime", entry);
    else
      return new ModelAndView("closest-stop-for-vehicle-xml.jspx", "stopTime", entry);
  }
}
