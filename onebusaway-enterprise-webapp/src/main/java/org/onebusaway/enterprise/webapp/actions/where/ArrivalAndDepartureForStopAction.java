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
package org.onebusaway.enterprise.webapp.actions.where;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.enterprise.webapp.actions.bundles.ArrivalAndDepartureMessages;
import org.onebusaway.presentation.impl.service_alerts.SituationsPresentation;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureForStopQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@Results({@Result(name = ActionSupport.ERROR, type="chain", location = "resource-not-found")})
public class ArrivalAndDepartureForStopAction extends AbstractWhereAction
    implements ModelDriven<ArrivalAndDepartureForStopQueryBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _service;

  private ArrivalsAndDeparturesPresentaion _presentation = new ArrivalsAndDeparturesPresentaion();

  private ArrivalAndDepartureForStopQueryBean _model = new ArrivalAndDepartureForStopQueryBean();

  private ArrivalAndDepartureBean _result;

  private SituationsPresentation _situations;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _service = transitDataService;
  }

  @Autowired
  public void setMessages(ArrivalAndDepartureMessages messages) {
    _presentation.setMessages(messages);
  }

  @Override
  public ArrivalAndDepartureForStopQueryBean getModel() {
    return _model;
  }

  public ArrivalAndDepartureBean getResult() {
    return _result;
  }

  public ArrivalsAndDeparturesPresentaion getPresentation() {
    return _presentation;
  }

  public SituationsPresentation getSituations() {
    if (_situations == null) {
      _situations = new SituationsPresentation();
      if (_result != null)
        _situations.setSituations(_result.getSituations());
      _situations.setUser(_currentUserService.getCurrentUser());
    }
    return _situations;
  }

  @Override
  @Actions({@Action(value = "/where/arrival-and-departure-for-stop")})
  public String execute() {

    if (_model.getTime() == 0)
      _model.setTime(SystemTime.currentTimeMillis());

    _result = _service.getArrivalAndDepartureForStop(_model);

    if (_result == null)
      return ERROR;

    return SUCCESS;
  }

}
