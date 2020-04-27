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

import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.AgencyAndIdInstance;
import org.onebusaway.transit_data_federation.impl.realtime.history.BlockLocationArchiveRecord;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/block-location-history-for-trip.action")
public class BlockLocationHistoryForTripController {

  @Autowired
  private BlockLocationHistoryService _service;

  @RequestMapping()
  public ModelAndView index(@RequestParam() String tripId) {

    AgencyAndId id = AgencyAndIdLibrary.convertFromString(tripId);

    Map<AgencyAndIdInstance, List<BlockLocationArchiveRecord>> histories = _service.getHistoryForTripId(id);

    ModelAndView mv = new ModelAndView("block-location-history-for-trip.jspx");
    mv.addObject("histories", histories);
    return mv;
  }
}
