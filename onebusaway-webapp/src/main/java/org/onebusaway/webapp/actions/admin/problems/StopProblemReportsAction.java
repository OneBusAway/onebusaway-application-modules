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
package org.onebusaway.webapp.actions.admin.problems;

import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.transit_data.model.problems.StopProblemReportBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

@Results({@Result(type = "redirectAction", name = "list", params = {
    "actionName", "stop-problem-reports", "stopId", "${model.stopId}", "parse",
    "true"})})
public class StopProblemReportsAction extends ActionSupport implements
    ModelDriven<StopProblemReportBean> {

  private static final long serialVersionUID = 1L;

  private TransitDataService _transitDataService;

  private List<StopProblemReportBean> _reports;

  private StopProblemReportBean _model = new StopProblemReportBean();

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public StopProblemReportBean getModel() {
    return _model;
  }

  public List<StopProblemReportBean> getReports() {
    return _reports;
  }

  @Override
  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "model.stopId", message = "missing tripId")})
  public String execute() {
    _reports = _transitDataService.getAllStopProblemReportsForStopId(_model.getStopId());
    return SUCCESS;
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "model.id", message = "missing id")}, requiredStrings = {@RequiredStringValidator(fieldName = "model.stopId", message = "missing stopId")})
  public String edit() {
    _model = _transitDataService.getStopProblemReportForStopIdAndId(
        _model.getStopId(), _model.getId());
    return "edit";
  }

  @Validations(requiredFields = {@RequiredFieldValidator(fieldName = "model.id", message = "missing id")}, requiredStrings = {@RequiredStringValidator(fieldName = "model.stopId", message = "missing stopId")})
  public String delete() {
    _transitDataService.deleteStopProblemReportForStopIdAndId(
        _model.getStopId(), _model.getId());

    return "list";
  }
}
