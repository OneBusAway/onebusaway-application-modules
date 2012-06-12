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
package org.onebusaway.webapp.actions.admin.console;

import java.io.IOException;
import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.JSONException;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@Results({@Result(type = "redirectAction", params = {
    "actionName", "service-alert", "id", "${id}", "parse", "true"})})
public class ServiceAlertAffectsAction extends ActionSupport implements
    ModelDriven<SituationAffectsBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private SituationAffectsBean _model = new SituationAffectsBean();

  private String _id;

  private int _index = -1;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public SituationAffectsBean getModel() {
    return _model;
  }

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public void setIndex(int index) {
    _index = index;
  }

  public int getIndex() {
    return _index;
  }

  public String update() throws IOException, JSONException {

    if (_id == null || _index == -1) {
      return INPUT;
    }

    ServiceAlertBean alert = _transitDataService.getServiceAlertForId(_id);
    if (alert == null) {
      return INPUT;
    }

    List<SituationAffectsBean> allAffects = alert.getAllAffects();
    if (allAffects == null || _index >= allAffects.size()) {
      return INPUT;
    }

    _model.setAgencyId(string(_model.getAgencyId()));
    _model.setRouteId(string(_model.getRouteId()));
    _model.setDirectionId(string(_model.getDirectionId()));
    _model.setTripId(string(_model.getTripId()));
    _model.setStopId(string(_model.getStopId()));
    
    allAffects.set(_index, _model);
    _transitDataService.updateServiceAlert(alert);

    return SUCCESS;
  }

  public String delete() {

    if (_id == null || _index == -1) {
      return INPUT;
    }

    ServiceAlertBean alert = _transitDataService.getServiceAlertForId(_id);
    if (alert == null) {
      return INPUT;
    }

    List<SituationAffectsBean> allAffects = alert.getAllAffects();
    if (allAffects == null || _index >= allAffects.size()) {
      return INPUT;
    }

    allAffects.remove(_index);
    _transitDataService.updateServiceAlert(alert);

    return SUCCESS;
  }

  /****
   * 
   ****/

  private String string(String value) {
    if (value == null || value.isEmpty() || value.equals("null"))
      return null;
    return value;
  }
}
