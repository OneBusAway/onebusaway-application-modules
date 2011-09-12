package org.onebusaway.transit_data_federation_webapp.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/service-ids.action")
public class ServiceIdsController {

  @Autowired
  private CalendarService _calendarService;

  @RequestMapping()
  public ModelAndView index() {

    Set<AgencyAndId> serviceIds = _calendarService.getServiceIds();
    List<String> ids = new ArrayList<String>();
    for (AgencyAndId serviceId : serviceIds)
      ids.add(AgencyAndIdLibrary.convertToString(serviceId));

    Collections.sort(ids);

    return new ModelAndView("service-ids.jsp", "ids", ids);
  }
}
