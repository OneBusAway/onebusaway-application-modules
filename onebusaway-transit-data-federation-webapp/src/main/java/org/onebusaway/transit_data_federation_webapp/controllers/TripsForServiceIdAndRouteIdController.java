package org.onebusaway.transit_data_federation_webapp.controllers;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.library.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/trips-for-service-id-and-route-id.action")
public class TripsForServiceIdAndRouteIdController {

  @Autowired
  private TransitDataFederationDao _dao;
  
  @Autowired
  private TripBeanService _tripBeanService;
  
  @RequestMapping()
  public ModelAndView index(@RequestParam() String serviceId, @RequestParam() String routeId) {

    AgencyAndId serviceAid = AgencyAndIdLibrary.convertFromString(serviceId);
    AgencyAndId routeAid = AgencyAndIdLibrary.convertFromString(routeId);
    List<AgencyAndId> tripIds = _dao.getTripIdsForServiceIdAndRouteCollectionId(serviceAid, routeAid);
    
    List<TripBean> trips = new ArrayList<TripBean>();
    for(AgencyAndId tripId : tripIds)
      trips.add(_tripBeanService.getTripForId(tripId));
    
    ModelAndView mv = new ModelAndView("trips-for-service-id-and-route-id.jsp");
    mv.addObject("serviceId",serviceId);
    mv.addObject("routeId",routeId);
    mv.addObject("trips", trips);
    return mv;
  }
}
