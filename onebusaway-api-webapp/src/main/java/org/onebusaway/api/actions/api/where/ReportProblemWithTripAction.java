/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.api.actions.api.where;

import java.io.IOException;
import java.util.Date;

import net.sf.json.JSONObject;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.TripProblemReportBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

public class ReportProblemWithTripAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private TripProblemReportBean _model = new TripProblemReportBean();

  private long _time = SystemTime.currentTimeMillis();

  public ReportProblemWithTripAction() {
    super(2);
  }

  @RequiredStringValidator(message = "requiredField.tripId")
  public void setId(String tripId) {
    _model.setTripId(tripId);
  }
  
  public void setTripId(String tripId) {
    setId(tripId);
  }

  public String getId() {
    return _model.getTripId();
  }

  public void setServiceDate(long serviceDate) {	   
    _model.setServiceDate(serviceDate);
  }

  public void setVehicleId(String vehicleId) {
    _model.setVehicleId(vehicleId);
  }

  public void setStopId(String stopId) {	
    _model.setStopId(stopId);
  }

  /**
   * @deprecated use {@link #setCode(String)} instead
   */
  @Deprecated
  public void setData(String data) {
    JSONObject json = JSONObject.fromObject(data);
    if (json.has("code")) {
      _model.setCode(json.getString("code"));
    }
  }

  public void setCode(String code) {
    _model.setCode(code);
  }

  public void setUserComment(String comment) {	  
    _model.setUserComment(comment);
  }

  public void setUserOnVehicle(boolean onVehicle) {
    _model.setUserOnVehicle(onVehicle);
  }

  public void setUserVehicleNumber(String vehicleNumber) {
    _model.setUserVehicleNumber(vehicleNumber);
  }

  public void setUserLat(double lat) {	 
    _model.setUserLat(lat);
  }

  public void setUserLon(double lon) {	
    _model.setUserLon(lon);
  }

  public void setUserLocationAccuracy(double userLocationAccuracy) {
    _model.setUserLocationAccuracy(userLocationAccuracy);
  }

  public DefaultHttpHeaders show() throws IOException, ServiceException {   
    return index();
  }

  public DefaultHttpHeaders create() throws IOException, ServiceException {	  
    return index();
  }

  @TypeConversion(converter = "org.onebusaway.presentation.impl.conversion.DateTimeConverter")
  public void setTime(Date time) {
    _time = time.getTime();
  }

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    if( ! isVersion(V2))
      return setUnknownVersionResponse();

    _model.setTime(_time);
    _model.setStatus(EProblemReportStatus.NEW);
    _service.reportProblemWithTrip(_model);

    return setOkResponse(new Object());
  }
}
