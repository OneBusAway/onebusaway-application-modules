package org.onebusaway.transit_data_federation_webapp.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.library.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/routes-for-service-id.action")
public class RoutesForServiceIdController {

  @Autowired
  private CalendarService _calendarService;

  @Autowired
  private TransitDataFederationDao _dao;

  @Autowired
  private RouteBeanService _routeBeanService;

  @RequestMapping()
  public ModelAndView index(@RequestParam() String serviceId) {

    AgencyAndId id = AgencyAndIdLibrary.convertFromString(serviceId);
    List<AgencyAndId> routeCollectionIds = _dao.getRouteCollectionIdsForServiceId(id);

    List<RouteBean> routes = new ArrayList<RouteBean>();
    for (AgencyAndId routeId : routeCollectionIds)
      routes.add(_routeBeanService.getRouteForId(routeId));

    List<ServiceDate> serviceDates = new ArrayList<ServiceDate>(
        _calendarService.getServiceDatesForServiceId(id));
    Collections.sort(serviceDates);
    
    List<Date> dates = new ArrayList<Date>();
    for( ServiceDate serviceDate : serviceDates)
      dates.add(serviceDate.getAsDate());

    ModelAndView mv = new ModelAndView("routes-for-service-id.jsp");
    mv.addObject("serviceId", serviceId);
    mv.addObject("routes", routes);
    mv.addObject("dates", dates);
    return mv;
  }
}
