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
package org.onebusaway.webapp.actions.admin.debug;

import java.util.Map;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

@Results({
    @Result(type = "redirectAction", name = "submitSuccess", params = {
        "actionName", "vehicle-location-record", "m", "1"}),
    @Result(type = "redirectAction", name = "resetSuccess", params = {
        "actionName", "vehicle-location-record", "m", "2"})})
public class VehicleLocationRecordAction extends ActionSupport implements
    ModelDriven<VehicleLocationRecordBean>, SessionAware {

  private static final String KEY_LAST_RECORD = VehicleLocationRecordAction.class.getName()
      + ".lastRecord";

  private static final long serialVersionUID = 1L;

  private VehicleLocationRecordBean _model = new VehicleLocationRecordBean();

  private TransitDataService _transitDataService;

  private Map<String, Object> _session;

  private int _m = 0;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public VehicleLocationRecordBean getModel() {
    return _model;
  }

  @Override
  public void setSession(Map<String, Object> session) {
    _session = session;
  }

  public void setM(int m) {
    _m = m;
  }

  @SkipValidation
  @Override
  public String execute() {
    VehicleLocationRecordBean lastRecord = (VehicleLocationRecordBean) _session.get(KEY_LAST_RECORD);
    if (lastRecord != null) {
      _model.copyFrom(lastRecord);
    }
    switch(_m) {
      case 1:
        addActionMessage("Record submitted!");
        break;
      case 2:
        addActionMessage("Vehicle reset!");
        break;
    }
    return SUCCESS;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "vehicleId", key = "requiredField")})
  public String submit() {

    if (_model.getTimeOfRecord() == 0)
      _model.setTimeOfRecord(System.currentTimeMillis());

    _model.setBlockId(clean(_model.getBlockId()));
    _model.setTripId(clean(_model.getTripId()));
    _model.setVehicleId(clean(_model.getVehicleId()));

    _transitDataService.submitVehicleLocation(_model);

    _session.put(KEY_LAST_RECORD, _model);

    return "submitSuccess";
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "vehicleId", key = "requiredField")})
  public String reset() {
    _transitDataService.resetVehicleLocation(_model.getVehicleId());
    return "resetSuccess";
  }

  private String clean(String value) {
    if (value != null && value.trim().isEmpty())
      return null;
    return value;
  }

}
