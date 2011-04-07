package org.onebusaway.transit_data_federation_webapp.controllers;

import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.AgencyAndIdInstance;
import org.onebusaway.transit_data_federation.impl.realtime.history.BlockLocationArchiveRecord;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
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
