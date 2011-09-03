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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
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
