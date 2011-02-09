package org.onebusaway.transit_data_federation_webapp.controllers;

import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data_federation.services.beans.StopsBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/stop-ids-for-agency-id.action")
public class StopIdsForAgencyIdController {

  @Autowired
  private StopsBeanService _service;

  @RequestMapping()
  public ModelAndView index(@RequestParam String agencyId) {
    ListBean<String> ids = _service.getStopsIdsForAgencyId(agencyId);
    return new ModelAndView("stop-ids.jspx", "ids", ids.getList());
  }
}
