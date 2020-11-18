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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.util.AgencyAndIdLibrary;
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
