/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.transit_data_federation.impl.bundle.BundleManagementServiceImpl;
import org.onebusaway.transit_data_federation.model.bundle.BundleItem;
import org.onebusaway.utility.DateLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BundleManagementController {

  @Autowired
  private BundleManagementServiceImpl _bundleManager;

  // for integration testing only
  @RequestMapping(value = "/change-bundle.do", method = RequestMethod.GET)
  public ModelAndView index(@RequestParam String bundleId, 
      @RequestParam(required=false) String time) throws Exception {

    if(time != null && !StringUtils.isEmpty(time)) {
      _bundleManager.setTime(DateLibrary.getIso8601StringAsTime(time));
    } else {
      _bundleManager.setTime(new Date());
    }

    _bundleManager.changeBundle(bundleId);

    return new ModelAndView("bundle-change.jspx");
  }

  @RequestMapping("/bundles.do")
  public ModelAndView index() {
    return new ModelAndView("bundles.jspx", "bms", _bundleManager);
  }

  @RequestMapping("/bundles!discover.do")
  public ModelAndView rediscover() throws Exception {
    _bundleManager.discoverBundles();

    return new ModelAndView("redirect:/bundles.do");
  }

  @RequestMapping("/bundles!reassign.do")
  public ModelAndView reassign(@RequestParam(required=false) String time) throws Exception {
    if(time != null && !StringUtils.isEmpty(time)) {
      _bundleManager.setTime(DateLibrary.getIso8601StringAsTime(time));
    } else {
      _bundleManager.setTime(new Date());
    }

    _bundleManager.refreshApplicableBundles();
    _bundleManager.reevaluateBundleAssignment();
    
    return new ModelAndView("redirect:/bundles.do");
  }

  @RequestMapping("/bundles!change.do")
  public ModelAndView change(@RequestParam String bundleId, @RequestParam(required=false) String time, @RequestParam(required=false) boolean automaticallySetDate) throws Exception {
    if(time != null && !StringUtils.isEmpty(time)) {
      _bundleManager.setTime(DateLibrary.getIso8601StringAsTime(time));
    } else {
      _bundleManager.setTime(new Date());
    }

    // if automaticallySetDate == true, we set the date to what it needs to be to have the bundle
    // change succeed
    if(automaticallySetDate == true) {
      List<BundleItem> bundles = _bundleManager.getAllKnownBundles();
      for(BundleItem bundle : bundles) {
        if(bundle.getId().equals(bundleId)) {
          Date targetDate = bundle.getServiceDateFrom().getAsDate();
          _bundleManager.setTime(targetDate);
          break;
        }
      }
    }
      
    _bundleManager.changeBundle(bundleId);
    
    return new ModelAndView("redirect:/bundles.do");
  }
}