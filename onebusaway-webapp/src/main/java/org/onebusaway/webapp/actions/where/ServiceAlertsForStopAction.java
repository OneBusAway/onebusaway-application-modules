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
package org.onebusaway.webapp.actions.where;

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean.AffectsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

public class ServiceAlertsForStopAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private List<String> _stopIds;

  private List<ServiceAlertBean> _situations;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @RequiredStringValidator(key = "requiredField")
  public void setStopId(List<String> stopIds) {
    _stopIds = stopIds;
  }

  public List<ServiceAlertBean> getSituations() {
    return _situations;
  }

  @Override
  @Actions({
      @Action(value = "/where/standard/service-alerts-for-stop"),
      @Action(value = "/where/iphone/service-alerts-for-stop"),
      @Action(value = "/where/text/service-alerts-for-stop")})
  public String execute() {
    SituationQueryBean query = new SituationQueryBean();
    for (String stopId : _stopIds) {
      AffectsBean affects = new SituationQueryBean.AffectsBean();
      affects.setStopId(stopId);
      query.getAffects().add(affects);
    }
    ListBean<ServiceAlertBean> list = _transitDataService.getServiceAlerts(query);
    _situations = list.getList();
    return SUCCESS;
  }
}
