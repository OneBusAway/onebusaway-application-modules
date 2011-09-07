package org.onebusaway.transit_data_federation_webapp.controllers;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data_federation.services.library.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/stop-ids.action")
public class StopIdsController {

  @Autowired
  private TransitGraphDao _graphDao;

  @RequestMapping()
  public ModelAndView index() {

    List<String> ids = new ArrayList<String>();

    for (StopEntry stop : _graphDao.getAllStops()) {
      String id = AgencyAndIdLibrary.convertToString(stop.getId());
      ids.add(id);
    }
    return new ModelAndView("stop-ids.jspx", "ids", ids);
  }
}
