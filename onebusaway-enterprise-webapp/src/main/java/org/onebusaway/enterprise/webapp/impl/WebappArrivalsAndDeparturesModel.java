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
package org.onebusaway.enterprise.webapp.impl;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.onebusaway.enterprise.webapp.actions.bundles.ArrivalAndDepartureMessages;
import org.onebusaway.enterprise.webapp.actions.where.ArrivalsAndDeparturesPresentaion;
import org.onebusaway.presentation.impl.ArrivalsAndDeparturesModel;
import org.onebusaway.presentation.impl.service_alerts.SituationsPresentation;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
public class WebappArrivalsAndDeparturesModel extends
    ArrivalsAndDeparturesModel {

  private ConfigurationService _configurationService;

  private ArrivalsAndDeparturesPresentaion _arrivalsAndDeparturesPresentation = new ArrivalsAndDeparturesPresentaion();

  private SituationsPresentation _situations;

  @Autowired
  public void setConfigurationService(ConfigurationService configurationService) {
    _configurationService = configurationService;
  }

  @Autowired
  public void setWhereMessages(ArrivalAndDepartureMessages messages) {
    _arrivalsAndDeparturesPresentation.setMessages(messages);
  }

  public ArrivalsAndDeparturesPresentaion getArrivalsAndDeparturesPresentation() {
    return _arrivalsAndDeparturesPresentation;
  }

  @Override
  public void setTargetTime(Date time) {
    super.setTargetTime(time);
    _arrivalsAndDeparturesPresentation.setTime(time.getTime());
  }

  public void setShowArrivals(boolean showArrivals) {
    super.setShowArrivals(showArrivals);
    _arrivalsAndDeparturesPresentation.setShowArrivals(showArrivals);
  }

  public boolean isShowArrivals() {
    return _arrivalsAndDeparturesPresentation.isShowArrivals();
  }

  public SituationsPresentation getSituations() {
    if (_situations == null) {
      _situations = new SituationsPresentation();

      HttpServletRequest request = ServletActionContext.getRequest();
      Map<String, String> config = _configurationService.getConfiguration();
      String key = (String) config.get("apiKey");
      if (key != null)
        _situations.setApiKey(key);

      _situations.setSituations(_result.getSituations());
      _situations.setUser(_user);
    }
    return _situations;
  }

  public SituationsPresentation getSituationsForArrivalAndDeparture(
      ArrivalAndDepartureBean arrivalAndDeparture) {
    SituationsPresentation situations = new SituationsPresentation();
    situations.setSituations(arrivalAndDeparture.getSituations());
    situations.setUser(_user);
    return situations;
  }

}
