package org.onebusaway.transit_data_federation_webapp.controllers;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransfer;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/transfers-for-stop.action")
public class TransfersForStopController {

  @Autowired
  private TransitGraphDao _graphDao;
  
  @Autowired
  private StopTransferService _stopTransferService;

  @RequestMapping()
  public ModelAndView index(@RequestParam() String stopId) {

    AgencyAndId id = AgencyAndIdLibrary.convertFromString(stopId);
    
    StopEntry stop = _graphDao.getStopEntryForId(id);
    List<StopTransfer> transfers = _stopTransferService.getTransfersFromStop(stop);

    ModelAndView mv = new ModelAndView("transfers-for-stop.jspx");
    mv.addObject("transfers", transfers);
    return mv;
  }
}
