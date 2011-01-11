/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.webapp.actions.where;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureForStopQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.gwt.where_library.WhereMessages;
import org.onebusaway.webapp.gwt.where_library.view.ArrivalsAndDeparturesPresentaion;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

public class ArrivalAndDepartureForStopAction extends AbstractWhereAction
    implements ModelDriven<ArrivalAndDepartureForStopQueryBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _service;

  private ArrivalsAndDeparturesPresentaion _presentation = new ArrivalsAndDeparturesPresentaion();

  private ArrivalAndDepartureForStopQueryBean _model = new ArrivalAndDepartureForStopQueryBean();

  private ArrivalAndDepartureBean _result;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _service = transitDataService;
  }

  @Autowired
  public void setWhereMessages(WhereMessages whereMessages) {
    _presentation.setMessages(whereMessages);
  }

  public ArrivalsAndDeparturesPresentaion getPresentation() {
    return _presentation;
  }

  @Override
  public ArrivalAndDepartureForStopQueryBean getModel() {
    return _model;
  }

  public ArrivalAndDepartureBean getResult() {
    return _result;
  }

  @Override
  @Actions({@Action(value = "/where/standard/arrival-and-departure-for-stop")})
  public String execute() {

    if (_model.getTime() == 0)
      _model.setTime(System.currentTimeMillis());

    _result = _service.getArrivalAndDepartureForStop(_model);

    if (_result == null)
      return ERROR;

    return SUCCESS;
  }
}
