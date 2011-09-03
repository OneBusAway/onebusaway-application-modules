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
