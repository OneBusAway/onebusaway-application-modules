package org.onebusaway.transit_data_federation_webapp.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/service-id.action")
public class ServiceIdController {

  @Autowired
  private CalendarService _calendarService;

  @RequestMapping()
  public ModelAndView index(@RequestParam() String serviceId) {

    AgencyAndId id = AgencyAndIdLibrary.convertFromString(serviceId);

    List<ServiceDate> serviceDates = new ArrayList<ServiceDate>(
        _calendarService.getServiceDatesForServiceId(id));
    Collections.sort(serviceDates);
    
    List<Date> dates = new ArrayList<Date>();
    for( ServiceDate serviceDate : serviceDates)
      dates.add(serviceDate.getAsDate());

    ModelAndView mv = new ModelAndView("service-id.jsp");
    mv.addObject("serviceId", serviceId);
    mv.addObject("dates", dates);
    return mv;
  }
}
