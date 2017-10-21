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
package org.onebusaway.api.actions.api.where;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.problems.EProblemReportStatus;
import org.onebusaway.transit_data.model.problems.StopProblemReportBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

public class ReportProblemWithStopAction extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private StopProblemReportBean _model = new StopProblemReportBean();

  public ReportProblemWithStopAction() {
    super(2);
  }

  @RequiredStringValidator(message = "requiredField.stopId")
  public void setId(String stopId) {
    _model.setStopId(stopId);
  }

  public String getId() {
    return _model.getStopId();
  }
  
  public void setStopId(String stopId) {
    setId(stopId);
  }

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

  public void setUserComment(String userComment) {
    _model.setUserComment(userComment);
  }

  public void setUserLat(double userLat) {
    _model.setUserLat(userLat);
  }

  public void setUserLon(double userLon) {
    _model.setUserLon(userLon);
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

  public DefaultHttpHeaders index() throws IOException, ServiceException {

    if (hasErrors())
      return setValidationErrorsResponse();

    if( ! isVersion(V2))
      return setUnknownVersionResponse();

    _model.setTime(SystemTime.currentTimeMillis());
    _model.setStatus(EProblemReportStatus.NEW);
    _service.reportProblemWithStop(_model);

    return setOkResponse(new Object());
  }
}
