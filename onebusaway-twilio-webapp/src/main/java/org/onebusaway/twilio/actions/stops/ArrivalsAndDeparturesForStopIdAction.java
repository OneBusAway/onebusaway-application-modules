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
package org.onebusaway.twilio.actions.stops;

import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.twilio.actions.TwilioSupport;
import org.onebusaway.twilio.impl.PhoneArrivalsAndDeparturesModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Results({
  @Result(name="success", location="arrivals-and-departures", type="redirectAction", params={"From", "${phoneNumber}"}),
  @Result(name="input", location="index", type="redirectAction")
})
public class ArrivalsAndDeparturesForStopIdAction extends TwilioSupport {

  private static Logger _log = LoggerFactory.getLogger(ArrivalsAndDeparturesForStopIdAction.class);
  
  private PhoneArrivalsAndDeparturesModel _model;

  @Autowired
  public void setModel(PhoneArrivalsAndDeparturesModel model) {
    _model = model;
  }

  public void setStopIds(List<String> stopIds) {
    _model.setStopIds(stopIds);
  }

  public void setRouteIds(Set<String> routeIds) {
    _model.setRouteFilter(routeIds);
  }

  public PhoneArrivalsAndDeparturesModel getModel() {
    return _model;
  }

  public String execute() throws Exception {
    _log.debug("in execute with stops=" + _model.getStopIds());
    // cleanup session if we chained into here
    sessionMap.remove("stop");
    if (_model.isMissingData()) {
      _log.warn("missing expected data");
      return INPUT;
    }

    _model.process();

    logUserInteraction("stopIds", _model.getStopIds(), "routeIds",
        _model.getRouteFilter());
    
    sessionMap.put("_model", _model);
    return SUCCESS;
  }
}
